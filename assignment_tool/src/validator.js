/**
 * Validator for FESUP 2026 Student Assignments.
 * Ensures all constraints are respected.
 */

class Validator {
    /**
     * Validates results against rooms and students.
     * @param {Array} rooms 
     * @param {Array} students 
     * @param {Object} results { schedules, planning }
     */
    static validate(rooms, students, results) {
        console.log('\n--- Starting Assignment Validation ---');
        const errors = [];
        const warnings = [];

        const { schedules, planning } = results;

        // 1. Session Count Check (MUST be exactly 4)
        students.forEach(student => {
            const schedule = schedules.get(student);
            const assignedCount = schedule.filter(s => s !== null).length;

            if (assignedCount !== 4) {
                errors.push(`Student ${student.firstName} ${student.lastName} has ${assignedCount} sessions (MUST be exactly 4).`);
            }
        });

        // 2. Room Capacity Check (with overflow warning)
        planning.forEach(session => {
            const room = rooms.find(r => r.id === session.roomId);
            if (!room) {
                errors.push(`Session at slot ${session.slot + 1} uses non-existent room ID: ${session.roomId}`);
                return;
            }

            // Error if exceeds max capacity (10% overflow)
            const maxCapacity = Math.floor(room.capacity * 1.1);
            if (session.students.length > maxCapacity) {
                errors.push(`Room ${room.id} max capacity (110%) exceeded in slot ${session.slot + 1}: ${session.students.length}/${maxCapacity}`);
            }

            // Warning if using overflow capacity
            if (session.students.length > room.capacity && session.students.length <= maxCapacity) {
                warnings.push(`Room ${room.id} using overflow capacity in slot ${session.slot + 1}: ${session.students.length}/${room.capacity} (base)`);
            }
        });

        // 3. Room Assignment Check (Every entry in roomPlanning must have roomId and presentation)
        planning.forEach((session, idx) => {
            if (!session.roomId) errors.push(`Session index ${idx} in planning has no roomId.`);
            if (!session.presentation) errors.push(`Session index ${idx} in planning has no presentation.`);
        });

        // 4. Wave/Slot Restrictions check
        students.forEach(student => {
            const schedule = schedules.get(student);
            const allowed = student.allowedSlots || []; // Should be set by parser

            schedule.forEach((presentation, slotIdx) => {
                if (presentation !== null) {
                    if (!allowed.includes(slotIdx)) {
                        errors.push(`Student ${student.lastName} assigned to Slot ${slotIdx + 1} but restricted to [${allowed.map(s => s + 1).join(', ')}].`);
                    }
                }
            });
        });

        // 5. Duplicate Presentations Check
        students.forEach(student => {
            const schedule = schedules.get(student);
            const presentations = schedule.filter(s => s !== null);
            const uniquePresentations = new Set(presentations);
            if (uniquePresentations.size !== presentations.length) {
                errors.push(`Student ${student.lastName} is assigned to the same presentation multiple times.`);
            }
        });

        // Summary report
        console.log(`- Students Checked: ${students.length}`);
        console.log(`- Sessions Checked: ${planning.length}`);

        if (errors.length === 0) {
            console.log('✅ VALIDATION PASSED: All constraints respected.');
        } else {
            console.error(`❌ VALIDATION FAILED: Found ${errors.length} errors.`);
            errors.slice(0, 10).forEach(e => console.error(`  - ${e}`));
            if (errors.length > 10) console.error(`  - ... and ${errors.length - 10} more errors.`);
        }

        if (warnings.length > 0) {
            console.warn(`⚠️ WARNINGS: Found ${warnings.length} issues.`);
            warnings.slice(0, 5).forEach(w => console.warn(`  - ${w}`));
        }

        return errors.length === 0;
    }
}

module.exports = Validator;
