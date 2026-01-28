package poc.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import poc.model.Etudiant;
import poc.model.Lycee;
import poc.model.Activite;
import poc.model.TypeActivite;
import poc.model.Voeu;
import poc.repository.ActiviteRepository;
import poc.repository.EtudiantRepository;
import poc.repository.LyceeRepository;
import poc.repository.ViewerRepository;

import java.io.*;
import java.util.*;

@Service
public class ExcelService {

    public void importStudents(String folderPath, EtudiantRepository etudiantRepo, LyceeRepository lyceeRepo) {
        DataFormatter fmt = new DataFormatter();
        Map<String, Lycee> cache = buildLyceeCache(lyceeRepo);

        for (File file : getExcelFiles(folderPath)) {
            try (FileInputStream fis = new FileInputStream(file);
                 Workbook wb = WorkbookFactory.create(fis)) {

                for (Row row : wb.getSheetAt(0)) {
                    if (row.getRowNum() == 0) continue;

                    String lycee = fmt.formatCellValue(row.getCell(0)).trim();
                    String nom = fmt.formatCellValue(row.getCell(1)).trim();
                    String prenom = fmt.formatCellValue(row.getCell(2)).trim();
                    String matricule = fmt.formatCellValue(row.getCell(3)).trim();
                    String classe = fmt.formatCellValue(row.getCell(4)).trim();

                    if (matricule.isEmpty() || nom.isEmpty() || matricule.contains("@")) continue;
                    if (etudiantRepo.findByMatriculeCsv(matricule).isPresent()) continue;

                    Etudiant e = new Etudiant();
                    e.setMatriculeCsv(matricule);
                    e.setNom(nom);
                    e.setPrenom(prenom);
                    e.setClasse(classe);
                    e.setLycee(getOrCreateLycee(lycee, cache, lyceeRepo));
                    e.setSerieBac("Generale");
                    e.setDemiJournee("DJ1");
                    etudiantRepo.save(e);
                }
            } catch (Exception ex) {
                System.err.println("Error importing " + file.getName() + ": " + ex.getMessage());
            }
        }
    }

    public void importViewers(String folderPath, ViewerRepository viewerRepo, LyceeRepository lyceeRepo) {
        DataFormatter fmt = new DataFormatter();
        Map<String, Lycee> cache = buildLyceeCache(lyceeRepo);

        for (File file : getExcelFiles(folderPath)) {
            try (FileInputStream fis = new FileInputStream(file);
                 Workbook wb = WorkbookFactory.create(fis)) {

                for (Row row : wb.getSheetAt(0)) {
                    if (row.getRowNum() == 0) continue;

                    String lycee = fmt.formatCellValue(row.getCell(0)).trim();
                    String nom = fmt.formatCellValue(row.getCell(1)).trim();
                    String prenom = fmt.formatCellValue(row.getCell(2)).trim();
                    String email = fmt.formatCellValue(row.getCell(3)).trim().toLowerCase();

                    if (email.isEmpty() || !email.contains("@")) continue;
                    if (viewerRepo.existsByEmail(email)) continue;

                    viewerRepo.save(new poc.model.Viewer(
                            email, "viewer123", nom, prenom,
                            getOrCreateLycee(lycee, cache, lyceeRepo)));
                }
            } catch (Exception ex) {
                System.err.println("Error importing viewers from " + file.getName() + ": " + ex.getMessage());
            }
        }
    }

    public void importActivities(String folderPath, ActiviteRepository activiteRepo) {
        File file = new File(folderPath + "/capacites.xlsx");
        if (!file.exists()) return;

        DataFormatter fmt = new DataFormatter();
        List<String> amphis = new ArrayList<>(), tds = new ArrayList<>();
        Map<String, Integer> caps = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook wb = WorkbookFactory.create(fis)) {

            for (Row row : wb.getSheetAt(0)) {
                if (row.getRowNum() == 0) continue;
                String salle = fmt.formatCellValue(row.getCell(0)).trim();
                String type = fmt.formatCellValue(row.getCell(2)).trim();
                if (salle.isEmpty()) continue;

                int cap = 30;
                try { cap = Integer.parseInt(fmt.formatCellValue(row.getCell(3)).trim()); } catch (Exception ignored) {}
                caps.put(salle, cap);
                (type.toLowerCase().contains("amphi") ? amphis : tds).add(salle);
            }
        } catch (Exception e) {
            System.err.println("Error reading rooms: " + e.getMessage());
            return;
        }

        String[] conferences = {
                "Etudes et metiers des arts, de la culture et du design",
                "Etudes et metiers du commerce", "Les etudes medicales",
                "Management economie gestion Licences CPGE",
                "Sciences et innovations technologiques BTS BUT",
                "Sociologie, sciences de l'education, histoire geographie Licences CPGE",
                "Etudes et metiers de l'informatique et du numerique",
                "Etudes et metiers du droit - Science Po",
                "Etudes et metiers du soin et de la sante",
                "Etudes et metiers du tourisme et de l'hotellerie BTS",
                "Etudes et metiers de l'habitat et de la construction",
                "Sciences et techniques Licence CPGE", "Etudes et metiers de l'ingenieur",
                "Etudes et metiers du management, economie, gestion (BTS/BUT)",
                "Etudes et metiers du secteur social", "Etudes et metiers du sport",
                "Lettres et langues Licences CPGE",
                "Sciences de la vie, de l'environnement et de l'agronomie BTS BUT",
                "Etre etudiant / Parcoursup"
        };

        String[] tablesRondes = {
                "Table ronde Etre etudiant en BTS", "Table ronde Etre etudiant en BUT",
                "Table ronde Etre etudiant en prepa CPI ou CPGE",
                "Table ronde Etre alternant dans l'enseignement superieur",
                "Table ronde Etre etudiant en Licences"
        };

        String[] flashMetiers = {
                "Flash metier ingenieur", "Flash metier social", "Flash metier commerce",
                "Flash metier sport", "Flash metier paramedical", "Flash metier design / architecture"
        };

        int ai = 0, ti = 0;
        for (String t : conferences) {
            String salle = !amphis.isEmpty() ? amphis.get(ai++ % amphis.size()) : (!tds.isEmpty() ? tds.get(ai++ % tds.size()) : "TBD");
            saveActivite(activiteRepo, "Conference " + t, TypeActivite.CONFERENCE, salle, caps);
        }
        for (String t : tablesRondes) {
            String salle = !tds.isEmpty() ? tds.get(ti++ % tds.size()) : "TBD";
            saveActivite(activiteRepo, t, TypeActivite.TABLE_RONDE, salle, caps);
        }
        for (String t : flashMetiers) {
            String salle = !tds.isEmpty() ? tds.get(ti++ % tds.size()) : "TBD";
            saveActivite(activiteRepo, t, TypeActivite.FLASH_METIER, salle, caps);
        }
    }

    public byte[] generateWishesExport(List<Activite> activites, List<Voeu> voeux) {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Map<Long, List<Voeu>> byActivite = new HashMap<>();
            for (Voeu v : voeux) byActivite.computeIfAbsent(v.getActivite().getId(), k -> new ArrayList<>()).add(v);

            for (Activite a : activites) {
                String name = a.getTitre().replaceAll("[^a-zA-Z0-9 ]", "").trim();
                if (name.length() > 30) name = name.substring(0, 30);
                if (name.isEmpty()) name = "Activite " + a.getId();
                int suffix = 1;
                String orig = name;
                while (wb.getSheet(name) != null) name = orig + " " + suffix++;

                Sheet sheet = wb.createSheet(name);
                Row info = sheet.createRow(0);
                info.createCell(0).setCellValue("Activite : " + a.getTitre());
                info.createCell(3).setCellValue("Salle : " + a.getSalle());
                info.createCell(5).setCellValue("Capacite : " + a.getNbPlaces());

                Row header = sheet.createRow(1);
                String[] cols = {"Matricule", "Nom", "Prenom", "Lycee", "Classe", "Demi-journee", "Voeu N"};
                for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);

                int idx = 2;
                for (Voeu v : byActivite.getOrDefault(a.getId(), List.of())) {
                    Row row = sheet.createRow(idx++);
                    Etudiant e = v.getEtudiant();
                    row.createCell(0).setCellValue(e.getMatriculeCsv());
                    row.createCell(1).setCellValue(e.getNom());
                    row.createCell(2).setCellValue(e.getPrenom());
                    row.createCell(3).setCellValue(e.getLycee() != null ? e.getLycee().getNom() : "");
                    row.createCell(4).setCellValue(e.getClasse());
                    row.createCell(5).setCellValue(e.getDemiJournee());
                    row.createCell(6).setCellValue(v.getPriorite());
                }
                for (int i = 0; i < 7; i++) sheet.autoSizeColumn(i);
            }

            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // --- Helpers ---

    private void saveActivite(ActiviteRepository repo, String titre, TypeActivite type, String salle, Map<String, Integer> caps) {
        Activite a = new Activite();
        a.setTitre(titre);
        a.setType(type);
        a.setSalle(salle);
        a.setNbPlaces(caps.getOrDefault(salle, 30));
        repo.save(a);
    }

    private List<File> getExcelFiles(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists()) return List.of();
        File[] files = folder.listFiles((d, n) -> (n.endsWith(".xlsx") || n.endsWith(".xls")) && !n.equalsIgnoreCase("capacites.xlsx"));
        return files != null ? List.of(files) : List.of();
    }

    private Map<String, Lycee> buildLyceeCache(LyceeRepository repo) {
        Map<String, Lycee> cache = new HashMap<>();
        repo.findAll().forEach(l -> cache.put(l.getNom().toUpperCase(), l));
        return cache;
    }

    private Lycee getOrCreateLycee(String name, Map<String, Lycee> cache, LyceeRepository repo) {
        Lycee lycee = cache.get(name.toUpperCase());
        if (lycee == null) {
            lycee = new Lycee();
            lycee.setNom(name);
            lycee = repo.save(lycee);
            cache.put(name.toUpperCase(), lycee);
        }
        return lycee;
    }
}
