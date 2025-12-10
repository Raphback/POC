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
                // Skip activities file
                if (file.getName().equalsIgnoreCase("capacites.xlsx")) continue;

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

        System.out.println("📂 Importing Activities (Mapping Titles to Rooms)...");
        DataFormatter formatter = new DataFormatter();

        // 1. Load all rooms from Excel
        java.util.List<String> amphis = new java.util.ArrayList<>();
        java.util.List<String> tds = new java.util.ArrayList<>();
        Map<String, Integer> roomCapacities = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String salle = formatter.formatCellValue(row.getCell(0)).trim();
                String type = formatter.formatCellValue(row.getCell(2)).trim();
                String capStr = formatter.formatCellValue(row.getCell(3)).trim();
                
                if (salle.isEmpty()) continue;

                int cap = 30;
                try { cap = Integer.parseInt(capStr); } catch (Exception e) {}
                roomCapacities.put(salle, cap);

                if (type.toLowerCase().contains("amphi")) {
                    amphis.add(salle);
                } else {
                    tds.add(salle);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading rooms: " + e.getMessage());
            return;
        }

        // 2. Define Titles (Hardcoded from DOCX)
        String[] conferences = {
            "Etudes et métiers des arts, de la culture et du design",
            "Etudes et métiers du commerce",
            "Les études médicales",
            "Management économie gestion Licences CPGE",
            "Sciences et innovations technologiques BTS BUT",
            "Sociologie, sciences de l’éducation, histoire géographie Licences CPGE",
            "Etudes et métiers de l’informatique et du numérique",
            "Etudes et métiers du droit - Science Po",
            "Etudes et métiers du soin et de la santé",
            "Etudes et métiers du tourisme et de l’hôtellerie BTS",
            "Etudes et métiers de l'habitat et de la construction",
            "Sciences et techniques Licence CPGE",
            "Etudes et métiers de l’ingénieur",
            "Etudes et métiers du management, économie, gestion (BTS/BUT)",
            "Etudes et métiers du secteur social",
            "Etudes et métiers du sport",
            "Lettres et langues Licences CPGE",
            "Sciences de la vie, de l’environnement et de l’agronomie BTS BUT",
            "Etre étudiant _ Parcoursup"
        };

        String[] others = {
            "Table ronde Etre étudiant en BTS (animation par des étudiants)",
            "Table ronde Etre étudiant en BUT (animation par des étudiants)",
            "Table ronde Etre étudiant en prépa CPI ou CPGE (animation par des étudiants)",
            "Table ronde Etre alternant dans l'enseignement supérieur (animation par des étudiants)",
            "Table ronde Etre étudiant en Licences (animation par des étudiants)",
            "Table ronde: flash métier \"ingénieur\" (animation par des professionnels)",
            "Table ronde: flash métier \"social\" (animation par des professionnels)",
            "Table ronde: flash métier \"commerce\" (animation par des professionnels)",
            "Table ronde: flash métier \"sport\" (animation par des professionnels)",
            "Table ronde: flash métier \"paramédical\" (animation par des professionnels)",
            "Table ronde: flash métier \"design / architecture\" (animation par des professionnels)"
        };

        // 3. Create Activities
        int amphiIndex = 0;
        int tdIndex = 0;

        // Conferences -> Amphis
        for (String title : conferences) {
            poc.model.Activite a = new poc.model.Activite();
            a.setTitre("Conférence " + title);
            a.setType(poc.model.TypeActivite.CONFERENCE);
            
            // Assign room (cycle if not enough)
            String salle = "Salle indéfinie";
            if (!amphis.isEmpty()) {
                salle = amphis.get(amphiIndex % amphis.size());
                amphiIndex++;
            } else if (!tds.isEmpty()) {
                 salle = tds.get(amphiIndex % tds.size()); // Fallback to TDs
                 amphiIndex++;
            }
            a.setSalle(salle);
            a.setNbPlaces(roomCapacities.getOrDefault(salle, 30));
            activiteRepository.save(a);
        }

        // Others -> TDs
        for (String title : others) {
            poc.model.Activite a = new poc.model.Activite();
            a.setTitre(title);
            
            if (title.toLowerCase().contains("flash")) {
                a.setType(poc.model.TypeActivite.FLASH_METIER);
            } else {
                a.setType(poc.model.TypeActivite.TABLE_RONDE);
            }

            // Assign room
            String salle = "Salle indéfinie";
            if (!tds.isEmpty()) {
                salle = tds.get(tdIndex % tds.size());
                tdIndex++;
            } else if (!amphis.isEmpty()) {
                salle = amphis.get(tdIndex % amphis.size()); // Fallback
                tdIndex++;
            }
            a.setSalle(salle);
            a.setNbPlaces(roomCapacities.getOrDefault(salle, 30));
            activiteRepository.save(a);
        }

        System.out.println("   ✅ Imported " + (conferences.length + others.length) + " activities with correct titles.");
    }
    public byte[] generateWishesExport(java.util.List<poc.model.Activite> activites, java.util.List<poc.model.Voeu> voeux) {
        try (Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
             java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {

            Map<Long, java.util.List<poc.model.Voeu>> voeuxByActivite = new HashMap<>();
            for (poc.model.Voeu v : voeux) {
                voeuxByActivite.computeIfAbsent(v.getActivite().getId(), k -> new java.util.ArrayList<>()).add(v);
            }

            for (poc.model.Activite activite : activites) {
                // Sanitize sheet name (max 31 chars, no special chars)
                String sheetName = activite.getTitre().replaceAll("[^a-zA-Z0-9 ]", "").trim();
                if (sheetName.length() > 30) sheetName = sheetName.substring(0, 30);
                if (sheetName.isEmpty()) sheetName = "Activite " + activite.getId();
                
                // Ensure unique sheet names
                int suffix = 1;
                String originalName = sheetName;
                while (workbook.getSheet(sheetName) != null) {
                    sheetName = originalName + " " + suffix++;
                }

                Sheet sheet = workbook.createSheet(sheetName);

                // Header
                Row headerInfo = sheet.createRow(0);
                headerInfo.createCell(0).setCellValue("Activité : " + activite.getTitre());
                headerInfo.createCell(3).setCellValue("Salle : " + activite.getSalle());
                headerInfo.createCell(5).setCellValue("Capacité : " + activite.getNbPlaces());

                Row header = sheet.createRow(1);
                header.createCell(0).setCellValue("Matricule");
                header.createCell(1).setCellValue("Nom");
                header.createCell(2).setCellValue("Prénom");
                header.createCell(3).setCellValue("Lycée");
                header.createCell(4).setCellValue("Classe");
                header.createCell(5).setCellValue("Demi-journée");
                header.createCell(6).setCellValue("Voeu N°");

                java.util.List<poc.model.Voeu> activityVoeux = voeuxByActivite.getOrDefault(activite.getId(), java.util.Collections.emptyList());
                
                int rowIdx = 2;
                for (poc.model.Voeu v : activityVoeux) {
                    Row row = sheet.createRow(rowIdx++);
                    Etudiant e = v.getEtudiant();
                    row.createCell(0).setCellValue(e.getMatriculeCsv());
                    row.createCell(1).setCellValue(e.getNom());
                    row.createCell(2).setCellValue(e.getPrenom());
                    row.createCell(3).setCellValue(e.getLycee() != null ? e.getLycee().getNom() : "");
                    row.createCell(4).setCellValue(e.getClasse());
                    row.createCell(5).setCellValue(e.getDemiJournee());
                    row.createCell(6).setCellValue(v.getPriorite());
                }
                
                // Auto-size columns
                for (int i = 0; i < 7; i++) {
                    sheet.autoSizeColumn(i);
                }
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

