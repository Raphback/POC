const XLSX = require('xlsx');

/**
 * Exports assignment results to an Excel file.
 * @param {string} filePath 
 * @param {Map} schedules student -> [s1, s2, ..., s20]
 * @param {Array} planning [{ slot, roomId, presentation, students }]
 * @param {Map} verificationReport student -> { status, wishDetails }
 */
function exportResults(filePath, schedules, planning, verificationReport = null) {
    const workbook = XLSX.utils.book_new();

    const getSlotLabel = (i) => {
        const day = i < 10 ? 'Jeudi' : 'Vendredi';
        const half = (i % 10) < 5 ? 'Matin' : 'Après-midi';
        const t = (i % 5) + 1;
        return `${day} ${half} T${t}`;
    };

    // 1. Student Schedules Sheet
    const studentRows = [];
    schedules.forEach((sessions, student) => {
        const row = {
            'Etablissement': student.establishment,
            'Nom': student.lastName,
            'Prénom': student.firstName,
            'Statut': verificationReport ? verificationReport.get(student).status : '',
            'Correspondance Voeux': verificationReport ? verificationReport.get(student).wishDetails : ''
        };
        for (let i = 0; i < sessions.length; i++) {
            row[getSlotLabel(i)] = sessions[i] || '';
        }
        studentRows.push(row);
    });
    const studentSheet = XLSX.utils.json_to_sheet(studentRows);
    XLSX.utils.book_append_sheet(workbook, studentSheet, 'Emplois du temps');

    // 2. Room Planning Sheet
    const planningRows = planning.map(p => ({
        'Créneau': getSlotLabel(p.slot),
        'Slot Index': p.slot, // Helper for sorting
        'Salle': p.roomId,
        'Présentation': p.presentation,
        'Nombre d\'élèves': p.students.length
    })).sort((a, b) => a['Slot Index'] - b['Slot Index'] || a.Salle.localeCompare(b.Salle));

    // Clean up helper column
    planningRows.forEach(r => delete r['Slot Index']);

    const planningSheet = XLSX.utils.json_to_sheet(planningRows);
    XLSX.utils.book_append_sheet(workbook, planningSheet, 'Planning Salles');

    XLSX.writeFile(workbook, filePath);
    console.log(`Results exported to ${filePath}`);
}

module.exports = {
    exportResults
};
