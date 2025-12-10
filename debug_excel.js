const XLSX = require('xlsx');
const workbook = XLSX.readFile('Inputs/capacites.xlsx');
const sheetName = workbook.SheetNames[0];
const sheet = workbook.Sheets[sheetName];
const data = XLSX.utils.sheet_to_json(sheet, { header: 1, range: 0, defval: '' });

console.log('Total Rows:', data.length);
let amphis = 0;
let tds = 0;
let others = 0;

data.forEach(row => {
    if (!row[2]) return;
    const type = row[2].toString().toLowerCase();
    if (type.includes('amphi')) amphis++;
    else if (type.includes('td')) tds++;
    else others++;
});

console.log('Amphis (Conferences):', amphis);
console.log('TDs (Flash/Tables?):', tds);
console.log('Others:', others);
