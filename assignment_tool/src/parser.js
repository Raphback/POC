const XLSX = require('xlsx');
const fs = require('fs');
const path = require('path');

/**
 * Parses the room capacities from an Excel file.
 * @param {string} filePath 
 * @returns {Array} List of room objects { id, building, type, capacity }
 */
function parseRooms(filePath) {
    const workbook = XLSX.readFile(filePath);
    const sheetName = workbook.SheetNames[0];
    const sheet = workbook.Sheets[sheetName];
    const data = XLSX.utils.sheet_to_json(sheet);

    return data.map(row => ({
        id: row['Salle N°'],
        building: row['Bâtiment'],
        type: row['type'] ? row['type'].trim() : 'Unknown',
        capacity: parseInt(row['capacité'], 10) || 0
    })).filter(room => room.id && room.capacity > 0);
}

/**
 * Parses school waves from the Flux Excel file.
 * @param {string} filePath 
 * @returns {Map} Map of School Name -> Array of Allowed Slot Indices
 */
function parseSchoolWaves(filePath) {
    const workbook = XLSX.readFile(filePath);
    // Use the exact sheet name we found earlier
    const sheet = workbook.Sheets['Réponses au formulaire 1'];
    const data = XLSX.utils.sheet_to_json(sheet, { header: 1 });

    const wavesMap = new Map();

    // Skip header row (index 0)
    for (let i = 1; i < data.length; i++) {
        const row = data[i];
        const schoolName = row[0];
        if (!schoolName) continue;

        // Column 7 is Voeu 1, Column 8 is Voeu 2
        const v1 = String(row[7] || '').toLowerCase();
        const v2 = String(row[8] || '').toLowerCase();

        const slots = new Set();

        const addSlots = (text) => {
            if (text.includes('jeudi') && text.includes('matin')) {
                // T1 to T5 (Indices 0 to 4)
                [0, 1, 2, 3, 4].forEach(s => slots.add(s));
            }
            if (text.includes('jeudi') && text.includes('après midi')) {
                // T6 to T10 (Indices 5 to 9)
                [5, 6, 7, 8, 9].forEach(s => slots.add(s));
            }
            if (text.includes('vendredi') && text.includes('matin')) {
                // T11 to T15 (Indices 10 to 14)
                [10, 11, 12, 13, 14].forEach(s => slots.add(s));
            }
            if (text.includes('vendredi') && text.includes('après midi')) {
                // T16 to T20 (Indices 15 to 19)
                [15, 16, 17, 18, 19].forEach(s => slots.add(s));
            }
        };

        addSlots(v1);
        addSlots(v2);

        // If no slots found (empty text?), default to something? 
        // Or assume they are everywhere? 
        // For safety, if empty, maybe default to Wave 2 (T2-T5) as per before?
        // But better to be strict if data is available.
        if (slots.size === 0) {
            // Fallback: If 'Fauriel' -> Morning (0-4)
            if (String(schoolName).includes('Fauriel')) {
                [0, 1, 2, 3, 4].forEach(s => slots.add(s));
            } else {
                // Default others to Afternoon (5-9) - or maybe both?
                // Let's default to Afternoon as per previous logic which favored later slots
                [5, 6, 7, 8, 9].forEach(s => slots.add(s));
            }
        }

        wavesMap.set(schoolName.trim(), Array.from(slots).sort((a, b) => a - b));
    }

    return wavesMap;
}

/**
 * Parses student wishes from a CSV or Excel file.
 * The format is expected to have student info and 5 wishes.
 * @param {string} filePath 
 * @param {Map} schoolWavesMap Optional map of school -> allowed slots
 * @returns {Array} List of student objects
 */
function parseStudentWishes(filePath, schoolWavesMap = null) {
    let rawData;
    if (filePath.endsWith('.csv')) {
        const workbook = XLSX.readFile(filePath);
        const sheetName = workbook.SheetNames[0];
        rawData = XLSX.utils.sheet_to_json(workbook.Sheets[sheetName]);
    } else {
        const workbook = XLSX.readFile(filePath);
        const sheetName = workbook.SheetNames[0];
        rawData = XLSX.utils.sheet_to_json(workbook.Sheets[sheetName]);
    }

    const studentsMap = new Map();

    rawData.forEach(row => {
        const key = `${row['Etablissement']}_${row['Nom de famille']}_${row['Prenom']}`;
        if (!studentsMap.has(key)) {
            const establishment = row['Etablissement'] ? row['Etablissement'].trim() : 'Unknown';
            let allowedSlots = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19]; // Default: All slots if not found

            if (schoolWavesMap) {
                const estLower = establishment.toLowerCase();
                // Find a match in the keys
                let foundMatch = false;
                for (let [fluxName, slots] of schoolWavesMap.entries()) {
                    const fluxLower = fluxName.toLowerCase();
                    if (estLower.includes(fluxLower) || fluxLower.includes(estLower)) {
                        allowedSlots = slots;
                        foundMatch = true;
                        break;
                    }
                }

                if (!foundMatch && establishment.includes('Fauriel')) {
                    allowedSlots = [0, 1, 2, 3, 4];
                }
            }

            studentsMap.set(key, {
                establishment,
                lastName: row['Nom de famille'],
                firstName: row['Prenom'],
                allowedSlots,
                wishes: []
            });
        }

        for (let i = 1; i <= 5; i++) {
            const wishKey = `Voeu ${i}`;
            if (row[wishKey]) {
                studentsMap.get(key).wishes[i - 1] = row[wishKey];
            }
        }

        if (row['WishNumber'] && row['WishTitle']) {
            const wishNum = parseInt(row['WishNumber'], 10);
            if (wishNum >= 1 && wishNum <= 5) {
                studentsMap.get(key).wishes[wishNum - 1] = row['WishTitle'];
            }
        }
    });

    return Array.from(studentsMap.values());
}

module.exports = {
    parseRooms,
    parseStudentWishes,
    parseSchoolWaves
};
