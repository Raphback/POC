/**
 * Detailed verifier for FESUP 2026 Student Assignments.
 */

class Verifier {
    /**
     * Performs a detailed check of assignments and wish correspondence.
     * @param {Array} students List of student objects
     * @param {Map} schedules student -> [s1, s2, ..., s20]
     * @returns {Map} student -> { status, wishDetails, assignedCount }
     */
    static detailedVerify(students, schedules) {
        const report = new Map();

        students.forEach(student => {
            const schedule = schedules.get(student);
            const assignedSessions = schedule.filter(s => s !== null);
            const assignedCount = assignedSessions.length;

            const status = assignedCount < 4 ? `KO: ${assignedCount}/4 séances` : "OK";

            const wishDetails = assignedSessions.map(session => {
                const wishIdx = student.wishes.findIndex(w => w === session);
                if (wishIdx !== -1) {
                    return `V${wishIdx + 1}`;
                } else {
                    return "Hors-voeux";
                }
            });

            report.set(student, {
                status,
                wishDetails: wishDetails.join(', '),
                assignedCount
            });
        });

        return report;
    }
}

module.exports = Verifier;
