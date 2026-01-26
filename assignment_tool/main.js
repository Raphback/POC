const path = require('path');
const { parseRooms, parseStudentWishes, parseSchoolWaves } = require('./src/parser');
const AssignmentAlgorithm = require('./src/algorithm');
const { exportResults } = require('./src/exporter');
const Validator = require('./src/validator');
const Verifier = require('./src/verifier');

const CONFIG = {
    roomsFile: path.resolve(__dirname, '../Inputs/capacites.xlsx'),
    studentsFile: path.resolve(__dirname, '../Inputs/Eleves_Voeux.csv'),
    fluxFile: path.resolve(__dirname, '../Inputs/FESUP du 26 & 27 mars 2026 - Flux des élèves.xlsx'),
    outputFile: path.resolve(__dirname, 'assignment_results.xlsx')
};

async function main() {
    try {
        console.log('--- FESUP 2026 Student Assignment Tool ---');

        // 1. Loading data
        console.log('Loading rooms...');
        const rooms = parseRooms(CONFIG.roomsFile);
        console.log(`Loaded ${rooms.length} rooms.`);

        console.log('Loading school waves from flux...');
        const schoolWaves = parseSchoolWaves(CONFIG.fluxFile);
        console.log(`Loaded waves for ${schoolWaves.size} schools.`);

        console.log('Loading students and wishes...');
        const students = parseStudentWishes(CONFIG.studentsFile, schoolWaves);
        console.log(`Loaded ${students.length} students.`);

        // 2. Running algorithm
        const algo = new AssignmentAlgorithm(rooms, students, 20);
        const results = algo.run();


        // 3. Validating results
        const isValid = Validator.validate(rooms, students, results);

        // 4. Detailed Verification
        console.log('Running detailed verification...');
        const verificationReport = Verifier.detailedVerify(students, results.schedules);

        // 5. Exporting results
        console.log('Exporting results...');
        exportResults(CONFIG.outputFile, results.schedules, results.planning, verificationReport);

        console.log('\n--- SUCCESS ---');
        console.log(`The assignment is complete.`);
        console.log(`- Results saved to: ${CONFIG.outputFile}`);
        console.log(`- Sheet 'Emplois du temps': Individual schedules for each student.`);
        console.log(`- Sheet 'Planning Salles': Global distribution of presentations per room and slot.`);

        if (!isValid) {
            console.log('\nNOTE: Some constraints could not be met due to physical capacity limits (see Validation Report above).');
        }

        console.log('\nDone!');
    } catch (error) {
        console.error('An error occurred:', error);
    }
}

main();
