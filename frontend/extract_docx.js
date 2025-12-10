const mammoth = require("mammoth");
const fs = require("fs");
const path = require("path");

const filePath = path.join(__dirname, "../Inputs/liste.docx");

mammoth.extractRawText({ path: filePath })
    .then(function (result) {
        fs.writeFileSync("docx_content.txt", result.value);
        console.log("Done writing to docx_content.txt");
    })
    .catch(function (err) {
        console.error(err);
    });
