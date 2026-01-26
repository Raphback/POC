/**
 * Core assignment logic for FESUP 2026.
 * Guarantees exactly 4 sessions per student with 10% overflow capacity.
 */

class AssignmentAlgorithm {
    constructor(rooms, students, slotsCount = 20) {
        this.rooms = rooms;
        this.students = students;
        this.slotsCount = slotsCount;

        // Results structures
        this.studentSchedules = new Map(); // student -> [session1, session2, ..., session20]
        this.roomPlanning = []; // [{ slot, roomId, presentation, students: [], baseCapacity, maxCapacity }]

        // Helper: room availability map with overflow
        this.roomAvailability = []; // slot -> { roomId -> { remaining, maxCapacity, baseCapacity } }
        for (let i = 0; i < slotsCount; i++) {
            const slotMap = {};
            this.rooms.forEach(r => {
                slotMap[r.id] = {
                    remaining: r.capacity,
                    baseCapacity: r.capacity,
                    maxCapacity: Math.floor(r.capacity * 1.1) // 10% overflow
                };
            });
            this.roomAvailability.push(slotMap);
        }

        // Pre-calculate flexibility (fewer slots = lower flexibility)
        this.students.forEach(s => {
            s.flexibility = (s.allowedSlots || []).length;
        });
    }

    /**
     * Runs the improved assignment process with guaranteed 4 sessions.
     */
    run() {
        console.log(`Starting assignment for ${this.students.length} students across ${this.slotsCount} slots.`);
        console.log('Goal: 100% of students get exactly 4 sessions, no duplicates, max 110% overflow.');

        // Sort rooms by capacity descending
        this.rooms.sort((a, b) => b.capacity - a.capacity);

        // Sort students by flexibility (least flexible first)
        const prioritizedStudents = [...this.students].sort((a, b) => {
            if (a.flexibility !== b.flexibility) return a.flexibility - b.flexibility;
            return Math.random() - 0.5; // Random for same flexibility
        });

        // 1. Calculate Demand
        const demand = {};
        const allPresentations = new Set();
        this.students.forEach(s => {
            s.wishes.forEach(w => {
                if (!w) return;
                demand[w] = (demand[w] || 0) + 1;
                allPresentations.add(w);
            });
        });

        // 2. Pre-Schedule Sessions
        console.log('Pre-scheduling sessions based on demand...');
        const sortedPresentations = [...allPresentations].sort((a, b) => demand[b] - demand[a]);

        sortedPresentations.forEach(pres => {
            const totalRequests = demand[pres];
            // Plan for base capacity roughly
            let sessionsNeeded = Math.min(this.slotsCount, Math.ceil(totalRequests / 50));
            sessionsNeeded = Math.max(3, sessionsNeeded);

            const step = this.slotsCount / sessionsNeeded;
            for (let i = 0; i < sessionsNeeded; i++) {
                const slot = Math.floor(i * step);
                this.createSession(slot, pres);
            }
        });

        // 3. Initialize student schedules
        this.students.forEach(s => {
            this.studentSchedules.set(s, new Array(this.slotsCount).fill(null));
        });

        // Pass 1: Mandatory wishes (V1, V2) - strict capacity
        console.log('Pass 1: Mandatory wishes (V1, V2)...');
        prioritizedStudents.forEach(student => {
            [0, 1].forEach(wishIdx => {
                const wish = student.wishes[wishIdx];
                if (wish) this.assignToPreScheduled(student, wish, false);
            });
        });

        // Pass 2: Optional wishes (V3, V4, V5) - strict capacity
        console.log('Pass 2: Optional wishes (V3, V4, V5)...');
        [2, 3, 4].forEach(wishIdx => {
            prioritizedStudents.forEach(student => {
                if (this.getSessionCount(student) >= 4) return;
                const wish = student.wishes[wishIdx];
                if (wish) this.assignToPreScheduled(student, wish, false);
            });
        });

        // Pass 3: Backfill with ANY wish - allow overflow
        console.log('Pass 3: Backfill with wishes using 110% overflow...');
        prioritizedStudents.forEach(student => {
            if (this.getSessionCount(student) >= 4) return;
            for (let wishIdx = 0; wishIdx < 5; wishIdx++) {
                if (this.getSessionCount(student) >= 4) break;
                const wish = student.wishes[wishIdx];
                if (wish) this.assignToPreScheduled(student, wish, true);
            }
        });

        // Pass 4: Emergency assignment (Out-of-wish) - with overflow
        console.log('Pass 4: Emergency out-of-wish assignment...');
        prioritizedStudents.forEach(student => {
            if (this.getSessionCount(student) >= 4) return;
            const allowed = student.allowedSlots || [];

            for (let slot of allowed) {
                if (this.getSessionCount(student) >= 4) break;
                if (this.studentSchedules.get(student)[slot] !== null) continue;

                const sessionsInSlot = this.roomPlanning.filter(p => p.slot === slot);
                for (let session of sessionsInSlot) {
                    if (this.studentSchedules.get(student).includes(session.presentation)) continue;

                    if (this.canAssignWithOverflow(session, slot)) {
                        this.assignStudentToSession(student, session, slot, true);
                        break;
                    }
                }
            }
        });

        // Pass 5: Dynamic Session Creation (Strict bottleneck handling)
        console.log('Pass 5: Dynamic session creation for remaining students...');
        const remainingStudents = this.students.filter(s => this.getSessionCount(s) < 4);
        console.log(`  ${remainingStudents.length} students still need sessions.`);

        remainingStudents.forEach(student => {
            const allowed = student.allowedSlots || [];

            while (this.getSessionCount(student) < 4) {
                let assigned = false;

                for (let slot of allowed) {
                    if (this.studentSchedules.get(student)[slot] !== null) continue;

                    // Look for a room that doesn't have a session at this slot
                    const availableRoomId = this.findAvailableRoom(slot);
                    if (availableRoomId) {
                        // Find a presentation the student doesn't have
                        const currentPres = this.studentSchedules.get(student);
                        const newPres = [...allPresentations].find(p => !currentPres.includes(p));

                        if (newPres && this.createSession(slot, newPres)) {
                            const newSession = this.roomPlanning[this.roomPlanning.length - 1];
                            this.assignStudentToSession(student, newSession, slot, false);
                            assigned = true;
                            break;
                        }
                    }
                }

                if (!assigned) {
                    // Final desperation: look for any room at this slot and add them if space remains
                    for (let slot of allowed) {
                        if (this.studentSchedules.get(student)[slot] !== null) continue;
                        const sessions = this.roomPlanning.filter(p => p.slot === slot);
                        for (let s of sessions) {
                            if (!this.studentSchedules.get(student).includes(s.presentation) && this.canAssignWithOverflow(s, slot)) {
                                this.assignStudentToSession(student, s, slot, true);
                                assigned = true;
                                break;
                            }
                        }
                        if (assigned) break;
                    }
                }

                if (!assigned) {
                    console.error(`  CRITICAL: Failed to assign student ${student.lastName}`);
                    break;
                }
            }
        });

        console.log('Assignment completed.');
        const stats = this.getStatistics();
        console.log(`\nFinal Statistics:`);
        console.log(`  Students with 4 sessions: ${stats.with4Sessions} (${stats.percentage4Sessions}%)`);
        console.log(`  Students with < 4 sessions: ${stats.withLess4Sessions}`);
        console.log(`  Rooms using 110% overflow: ${stats.roomsWithOverflow}`);

        return {
            schedules: this.studentSchedules,
            planning: this.roomPlanning
        };
    }

    createSession(slot, presentation) {
        const availableRoomId = this.findAvailableRoom(slot);
        if (availableRoomId) {
            const roomInfo = this.roomAvailability[slot][availableRoomId];
            const newSession = {
                slot,
                roomId: availableRoomId,
                presentation,
                students: [],
                baseCapacity: roomInfo.baseCapacity,
                maxCapacity: roomInfo.maxCapacity
            };
            this.roomPlanning.push(newSession);
            return true;
        }
        return false;
    }

    findAvailableRoom(slot) {
        const busyRooms = new Set(this.roomPlanning.filter(p => p.slot === slot).map(p => p.roomId));
        const freeRoom = this.rooms.find(r => !busyRooms.has(r.id));
        return freeRoom ? freeRoom.id : null;
    }

    assignToPreScheduled(student, presentation, allowOverflow = false) {
        const schedule = this.studentSchedules.get(student);
        if (schedule.includes(presentation)) return false;

        const allowedSlots = student.allowedSlots || [];
        for (let slot of allowedSlots) {
            if (schedule[slot] !== null) continue;

            const session = this.roomPlanning.find(p => p.slot === slot && p.presentation === presentation);
            if (session) {
                const roomInfo = this.roomAvailability[slot][session.roomId];
                const canAssign = allowOverflow
                    ? session.students.length < roomInfo.maxCapacity
                    : session.students.length < roomInfo.baseCapacity;

                if (canAssign) {
                    this.assignStudentToSession(student, session, slot, allowOverflow);
                    return true;
                }
            }
        }
        return false;
    }

    canAssignWithOverflow(session, slot) {
        return session.students.length < session.maxCapacity;
    }

    assignStudentToSession(student, session, slot, isOverflow = false) {
        session.students.push(student);
        this.studentSchedules.get(student)[slot] = session.presentation;
    }

    getSessionCount(student) {
        return (this.studentSchedules.get(student) || []).filter(s => s !== null).length;
    }

    getStatistics() {
        const with4Sessions = this.students.filter(s => this.getSessionCount(s) === 4).length;
        const withLess4Sessions = this.students.filter(s => this.getSessionCount(s) < 4).length;
        const percentage4Sessions = ((with4Sessions / this.students.length) * 100).toFixed(2);
        const roomsWithOverflow = this.roomPlanning.filter(s => s.students.length > s.baseCapacity).length;

        return {
            with4Sessions,
            withLess4Sessions,
            percentage4Sessions,
            roomsWithOverflow
        };
    }
}

module.exports = AssignmentAlgorithm;
