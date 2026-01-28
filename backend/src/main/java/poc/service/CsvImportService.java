package poc.service;

import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import poc.model.Etudiant;
import poc.model.Lycee;
import poc.repository.EtudiantRepository;
import poc.repository.LyceeRepository;

import java.util.Iterator;

@Service
public class CsvImportService {

    @Autowired private EtudiantRepository etudiantRepository;
    @Autowired private LyceeRepository lyceeRepository;

    private final DataFormatter fmt = new DataFormatter();

    public void importerEleves(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xls") && !filename.endsWith(".xlsx"))) {
            throw new RuntimeException("Format non supporte (attendu : .xls ou .xlsx)");
        }

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Iterator<Row> rows = workbook.getSheetAt(0).iterator();
            if (!rows.hasNext()) return;

            String header = getRowAsString(rows.next());
            if (header.contains("INE")) {
                importFormatBrassens(rows);
            } else if (header.contains("Division")) {
                importFormatFauriel(rows);
            } else {
                throw new RuntimeException("Format de colonnes inconnu.");
            }
        }
    }

    private void importFormatBrassens(Iterator<Row> rows) {
        while (rows.hasNext()) {
            Row row = rows.next();
            if (isRowEmpty(row)) continue;
            sauvegarder(cell(row, 3), cell(row, 1), cell(row, 2),
                    cell(row, 0), cell(row, 4), "Generale", cell(row, 5));
        }
    }

    private void importFormatFauriel(Iterator<Row> rows) {
        while (rows.hasNext()) {
            Row row = rows.next();
            if (isRowEmpty(row)) continue;
            String nom = cell(row, 0), prenom = cell(row, 1);
            String matricule = "FAURIEL_" + nom.toUpperCase().replaceAll("[^A-Z]", "")
                    + "_" + prenom.toUpperCase().replaceAll("[^A-Z]", "");
            sauvegarder(matricule, nom, prenom, "LGT Fauriel", cell(row, 5), cell(row, 6), cell(row, 8));
        }
    }

    private void sauvegarder(String matricule, String nom, String prenom,
                             String nomLycee, String classe, String serie, String demiJournee) {
        if (matricule == null || matricule.isEmpty()) return;

        Lycee lycee = lyceeRepository.findByNom(nomLycee)
                .orElseGet(() -> { Lycee l = new Lycee(); l.setNom(nomLycee); return lyceeRepository.save(l); });

        Etudiant e = etudiantRepository.findByMatriculeCsv(matricule).orElse(new Etudiant());
        e.setMatriculeCsv(matricule);
        e.setNom(nom);
        e.setPrenom(prenom);
        e.setLycee(lycee);
        e.setClasse(classe);
        e.setSerieBac(serie);
        if (demiJournee != null && !demiJournee.isEmpty()) e.setDemiJournee(demiJournee);
        etudiantRepository.save(e);
    }

    private String cell(Row row, int index) {
        Cell c = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return c == null ? "" : fmt.formatCellValue(c).trim();
    }

    private String getRowAsString(Row row) {
        StringBuilder sb = new StringBuilder();
        for (Cell c : row) sb.append(cell(row, c.getColumnIndex())).append(" ");
        return sb.toString();
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) return false;
        }
        return true;
    }
}
