package poc.service;

import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import poc.model.Etudiant;
import poc.model.Lycee;
import poc.repository.EtudiantRepository;
import poc.repository.LyceeRepository;

import java.io.InputStream;
import java.util.Iterator;

@Service
public class CsvImportService {

    @Autowired
    private EtudiantRepository etudiantRepository;

    @Autowired
    private LyceeRepository lyceeRepository;

    public void importerEleves(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        if (filename != null && (filename.endsWith(".xls") || filename.endsWith(".xlsx"))) {
            importerExcel(file.getInputStream());
        } else {
            throw new RuntimeException("Format de fichier non supporté (attendu : .xls ou .xlsx)");
        }
    }

    private void importerExcel(InputStream inputStream) throws Exception {
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();

        if (!rowIterator.hasNext()) return;

        // Analyse du Header pour déterminer le format
        Row headerRow = rowIterator.next();
        String headerString = getRowAsString(headerRow);

        if (headerString.contains("INE")) {
            importerFormatBrassens(rowIterator);
        } else if (headerString.contains("Division")) {
            importerFormatFauriel(rowIterator);
        } else {
            throw new RuntimeException("Format de colonnes inconnu. Vérifiez les en-têtes.");
        }
        workbook.close();
    }

    private void importerFormatBrassens(Iterator<Row> rowIterator) {
        // Colonnes : Etablissement, Nom, Prénom, INE, Classe
        // Adaptation: On suppose que la colonne 5 ou 6 pourrait être "Demi-journée" si elle existe
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (isRowEmpty(row)) continue;

            String nomLycee = getCellValue(row, 0);
            String nom = getCellValue(row, 1);
            String prenom = getCellValue(row, 2);
            String ine = getCellValue(row, 3);
            String classe = getCellValue(row, 4);
            // On tente de lire une potentielle colonne demi-journée (index 5)
            String demiJournee = getCellValue(row, 5);

            sauvegarderEtudiant(ine, nom, prenom, nomLycee, classe, "Générale", demiJournee);
        }
    }

    private void importerFormatFauriel(Iterator<Row> rowIterator) {
        // Colonnes : Nom, Prénom, Date de naissance, Sexe, Regime, Division, MEF, Options
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (isRowEmpty(row)) continue;

            String nom = getCellValue(row, 0);
            String prenom = getCellValue(row, 1);
            String division = getCellValue(row, 5); // Classe
            String mef = getCellValue(row, 6);      // Série
            // On tente de lire une potentielle colonne demi-journée (index 8 par exemple, ou à la fin)
            // Pour l'instant on laisse vide ou on prend index 8 si existe
            String demiJournee = getCellValue(row, 8);

            // Génération d'un ID technique car pas d'INE
            String matricule = "FAURIEL_" + nom.toUpperCase().replaceAll("[^A-Z]", "") + "_" + prenom.toUpperCase().replaceAll("[^A-Z]", "");
            
            sauvegarderEtudiant(matricule, nom, prenom, "LGT Fauriel", division, mef, demiJournee);
        }
    }

    private void sauvegarderEtudiant(String matricule, String nom, String prenom, String nomLycee, String classe, String serie, String demiJournee) {
        if (matricule == null || matricule.isEmpty()) return;

        // Gestion du Lycée
        Lycee lycee = lyceeRepository.findByNom(nomLycee)
                .orElseGet(() -> {
                    Lycee newLycee = new Lycee();
                    newLycee.setNom(nomLycee);
                    return lyceeRepository.save(newLycee);
                });

        // Gestion de l'Étudiant
        Etudiant etudiant = etudiantRepository.findByMatriculeCsv(matricule)
                .orElse(new Etudiant());

        etudiant.setMatriculeCsv(matricule);
        etudiant.setNom(nom);
        etudiant.setPrenom(prenom);
        etudiant.setLycee(lycee);
        etudiant.setClasse(classe);
        etudiant.setSerieBac(serie);
        if (demiJournee != null && !demiJournee.isEmpty()) {
            etudiant.setDemiJournee(demiJournee);
        }

        etudiantRepository.save(etudiant);
    }

    private String getCellValue(Row row, int index) {
        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";
        return new DataFormatter().formatCellValue(cell).trim();
    }

    private String getRowAsString(Row row) {
        StringBuilder sb = new StringBuilder();
        for (Cell cell : row) {
            sb.append(getCellValue(row, cell.getColumnIndex())).append(" ");
        }
        return sb.toString();
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK)
                return false;
        }
        return true;
    }
}
