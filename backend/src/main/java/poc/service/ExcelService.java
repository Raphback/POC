package poc.service;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import poc.model.Etudiant;
import poc.model.Lycee;
import poc.repository.EtudiantRepository;
import poc.repository.LyceeRepository;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class ExcelService {

    public void importStudents(String folderPath, EtudiantRepository etudiantRepository, LyceeRepository lyceeRepository) {
        File folder = new File(folderPath);
        if (!folder.exists()) {
            System.out.println("❌ Folder not found: " + folderPath);
            return;
        }

        // Cache for Lycees to avoid duplicates
        Map<String, Lycee> lyceeCache = new HashMap<>();
        lyceeRepository.findAll().forEach(l -> lyceeCache.put(l.getNom().toUpperCase(), l));

        File[] files = folder.listFiles();
        if (files == null) return;
        
        DataFormatter formatter = new DataFormatter();

        for (File file : files) {
            if (file.getName().endsWith(".xlsx") || file.getName().endsWith(".xls")) {
                System.out.println("📂 Importing: " + file.getName());
                try (FileInputStream fis = new FileInputStream(file);
                     Workbook workbook = WorkbookFactory.create(fis)) {
                    
                    Sheet sheet = workbook.getSheetAt(0);
                    int count = 0;
                    for (Row row : sheet) {
                        if (row.getRowNum() == 0) continue; // Skip header

                        String lyceeName = formatter.formatCellValue(row.getCell(0)).trim();
                        String nom = formatter.formatCellValue(row.getCell(1)).trim();
                        String prenom = formatter.formatCellValue(row.getCell(2)).trim();
                        String matricule = formatter.formatCellValue(row.getCell(3)).trim();
                        String classe = formatter.formatCellValue(row.getCell(4)).trim();

                        if (matricule.isEmpty() || nom.isEmpty()) continue;

                        // Manage Lycee
                        Lycee lycee = lyceeCache.get(lyceeName.toUpperCase());
                        if (lycee == null) {
                            lycee = new Lycee();
                            lycee.setNom(lyceeName);
                            lycee = lyceeRepository.save(lycee);
                            lyceeCache.put(lyceeName.toUpperCase(), lycee);
                            System.out.println("   🏫 New Lycee created: " + lyceeName);
                        }

                        // Check if student already exists
                        if (etudiantRepository.findByMatriculeCsv(matricule).isPresent()) {
                            continue;
                        }

                        // Create Student
                        Etudiant etudiant = new Etudiant();
                        etudiant.setMatriculeCsv(matricule);
                        etudiant.setNom(nom);
                        etudiant.setPrenom(prenom);
                        etudiant.setClasse(classe);
                        etudiant.setLycee(lycee);
                        etudiant.setSerieBac("Générale"); // Defaulting for now
                        etudiant.setDemiJournee("DJ1"); // Default

                        etudiantRepository.save(etudiant);
                        count++;
                    }
                    System.out.println("   ✅ Imported " + count + " students from " + file.getName());

                } catch (Exception e) {
                    System.err.println("Error importing " + file.getName() + ": " + e.getMessage());
                }
            }
        }
    }
    
    // Keep inspectHeaders for debugging if needed
    public void inspectHeaders(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists()) {
            System.out.println("❌ Folder not found: " + folderPath);
            return;
        }

        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.getName().endsWith(".xlsx") || file.getName().endsWith(".xls")) {
                System.out.println("📂 Inspecting: " + file.getName());
                try (FileInputStream fis = new FileInputStream(file);
                     Workbook workbook = WorkbookFactory.create(fis)) {
                    
                    Sheet sheet = workbook.getSheetAt(0);
                    Row headerRow = sheet.getRow(0);
                    if (headerRow != null) {
                        System.out.print("   Headers: ");
                        for (Cell cell : headerRow) {
                            System.out.print("[" + cell.toString() + "] ");
                        }
                        System.out.println();
                    }
                    
                    Row dataRow = sheet.getRow(1);
                    if (dataRow != null) {
                        System.out.print("   Row 1: ");
                        for (Cell cell : dataRow) {
                            System.out.print("[" + cell.toString() + "] ");
                        }
                        System.out.println();
                    }
                } catch (Exception e) {
                    System.err.println("Error reading " + file.getName() + ": " + e.getMessage());
                }
            }
        }
    }

    public void importActivities(String folderPath, poc.repository.ActiviteRepository activiteRepository) {
        File file = new File(folderPath + "/capacites.xlsx");
        if (!file.exists()) {
            System.out.println("❌ Activities file not found: " + file.getAbsolutePath());
            return;
        }

        System.out.println("📂 Importing Activities from: " + file.getName());
        DataFormatter formatter = new DataFormatter();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            int count = 0;
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header

                String titre = formatter.formatCellValue(row.getCell(0)).trim();
                String salle = formatter.formatCellValue(row.getCell(1)).trim();
                String capStr = formatter.formatCellValue(row.getCell(2)).trim();

                if (titre.isEmpty()) continue;

                poc.model.Activite activite = new poc.model.Activite();
                activite.setTitre(titre);
                activite.setSalle(salle);
                
                if (titre.toLowerCase().contains("conférence")) {
                    activite.setType(poc.model.TypeActivite.CONFERENCE);
                } else if (titre.toLowerCase().contains("table ronde")) {
                    activite.setType(poc.model.TypeActivite.TABLE_RONDE);
                } else {
                    activite.setType(poc.model.TypeActivite.FLASH_METIER);
                }

                try {
                    activite.setNbPlaces(Integer.parseInt(capStr));
                } catch (NumberFormatException e) {
                    activite.setNbPlaces(30);
                }

                activiteRepository.save(activite);
                count++;
            }
            System.out.println("   ✅ Imported " + count + " activities.");

        } catch (Exception e) {
            System.err.println("Error importing activities: " + e.getMessage());
        }
    }
}

