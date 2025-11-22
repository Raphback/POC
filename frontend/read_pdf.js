const fs = require('fs');
const pdf = require('pdf-parse');

const filePath = './doc.pdf';

if (!fs.existsSync(filePath)) {
    console.error('File not found:', filePath);
    process.exit(1);
}

let dataBuffer = fs.readFileSync(filePath);

pdf(dataBuffer).then(function (data) {
    console.log(data.text);
}).catch(function (error) {
    console.error('Error parsing PDF:', error);
});
