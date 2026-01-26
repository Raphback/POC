const path = require('path');
const { parseRooms, parseStudentWishes, parseSchoolWaves } = require('./src/parser');

const CONFIG = {
    roomsFile: path.resolve(__dirname, '../Inputs/capacites.xlsx'),
    studentsFile: path.resolve(__dirname, '../Inputs/Eleves_Voeux.csv'),
    fluxFile: path.resolve(__dirname, '../Inputs/FESUP du 26 & 27 mars 2026 - Flux des élèves.xlsx'),
};

try {
    console.log('Testing Parser...');
    console.log('1. Rooms');
    const rooms = parseRooms(CONFIG.roomsFile);
    console.log('Rooms:', rooms.length);

    console.log('2. Waves');
    const waves = parseSchoolWaves(CONFIG.fluxFile);
    console.log('Waves:', waves.size);

    console.log('3. Students');
    const students = parseStudentWishes(CONFIG.studentsFile, waves);
    console.log('Students:', students.length);
    if (students.length > 0) {
        console.log('Sample Student:', students[0]);
    }

} catch (err) {
    console.error('Debug Error:', err);
}
