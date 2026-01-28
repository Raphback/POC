package poc.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

import poc.model.*;
import poc.repository.*;
import poc.service.ExcelService;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(
            ActiviteRepository activiteRepo, LyceeRepository lyceeRepo,
            AdminRepository adminRepo, ViewerRepository viewerRepo,
            EtudiantRepository etudiantRepo, AffectationRepository affectationRepo,
            VoeuRepository voeuRepo, JdbcTemplate jdbc, ExcelService excelService) {

        return args -> {
            initAdmin(adminRepo);

            // Si des fichiers Excel existent dans Inputs/, on les utilise
            File inputsFolder = new File("Inputs");
            boolean hasExcel = inputsFolder.exists() && inputsFolder.isDirectory()
                    && inputsFolder.listFiles((d, n) -> n.endsWith(".xlsx")) != null
                    && inputsFolder.listFiles((d, n) -> n.endsWith(".xlsx")).length > 0;

            if (hasExcel) {
                // Reset et import depuis fichiers Excel
                affectationRepo.deleteAll();
                voeuRepo.deleteAll();
                etudiantRepo.deleteAll();
                activiteRepo.deleteAll();
                viewerRepo.deleteAll();

                excelService.importActivities("Inputs", activiteRepo);
                excelService.importStudents("Inputs", etudiantRepo, lyceeRepo);
                excelService.importViewers("Inputs", viewerRepo, lyceeRepo);
            } else {
                // Mode test : donnees hardcodees
                Lycee fauriel = getOrCreateLycee(lyceeRepo, "Fauriel", "Lycee Claude Fauriel");
                Lycee brassens = getOrCreateLycee(lyceeRepo, "Brassens", "Lycee Georges Brassens");
                initViewers(viewerRepo, fauriel, brassens);
                cleanDuplicateINE(jdbc);
                executeSqlFile(jdbc);
                initActivites(activiteRepo);
            }

            System.out.println("===========================================");
            System.out.println("  Etudiants: " + etudiantRepo.count());
            System.out.println("  Viewers:   " + viewerRepo.count());
            System.out.println("  Activites: " + activiteRepo.count());
            System.out.println("  ADMIN: admin / admin");
            System.out.println("===========================================");
        };
    }

    private void initAdmin(AdminRepository repo) {
        if (repo.findByUsername("admin").isEmpty()) {
            repo.save(new Admin("admin", "admin", Admin.Role.SUPER_ADMIN));
        }
    }

    private Lycee getOrCreateLycee(LyceeRepository repo, String search, String fullName) {
        return repo.findAll().stream()
                .filter(l -> l.getNom().contains(search))
                .findFirst()
                .orElseGet(() -> {
                    Lycee l = new Lycee();
                    l.setNom(fullName);
                    return repo.save(l);
                });
    }

    private void initViewers(ViewerRepository repo, Lycee fauriel, Lycee brassens) {
        if (repo.findByEmail("prof@fauriel.fr").isEmpty())
            repo.save(new Viewer("prof@fauriel.fr", "prof", "Dupont", "Jean", fauriel));
        if (repo.findByEmail("prof@brassens.fr").isEmpty())
            repo.save(new Viewer("prof@brassens.fr", "prof", "Martin", "Marie", brassens));
    }

    private void cleanDuplicateINE(JdbcTemplate jdbc) {
        try {
            List<String> dupes = jdbc.queryForList(
                    "SELECT ine FROM etudiant WHERE ine IS NOT NULL GROUP BY ine HAVING COUNT(*) > 1", String.class);
            for (String ine : dupes) {
                jdbc.update("DELETE FROM etudiant WHERE ine = ? AND id NOT IN (SELECT MIN(id) FROM etudiant WHERE ine = ?)", ine, ine);
            }
        } catch (Exception ignored) {}
    }

    private void executeSqlFile(JdbcTemplate jdbc) {
        try {
            ClassPathResource res = new ClassPathResource("data.sql");
            if (res.exists()) {
                String sql = new String(res.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                for (String stmt : sql.split(";")) {
                    stmt = stmt.trim();
                    if (!stmt.isEmpty() && !stmt.startsWith("--")) {
                        try { jdbc.execute(stmt); } catch (Exception ignored) {}
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private void initActivites(ActiviteRepository repo) {
        if (repo.count() > 0) return;

        String[][] confs = {
                {"Etudes et metiers des arts, de la culture et du design", "Amphi A"},
                {"Etudes et metiers du commerce", "Amphi B"},
                {"Les etudes medicales", "Amphi C"},
                {"Management economie gestion Licences CPGE", "Amphi D"},
                {"Sciences et innovations technologiques BTS BUT", "Amphi E"},
                {"Sociologie, sciences de l'education, histoire geographie Licences CPGE", "Amphi A"},
                {"Etudes et metiers de l'informatique et du numerique", "Amphi B"},
                {"Etudes et metiers du droit - Science Po", "Amphi C"},
                {"Etudes et metiers du soin et de la sante", "Amphi D"},
                {"Etudes et metiers du tourisme et de l'hotellerie BTS", "Amphi E"},
                {"Etudes et metiers de l'habitat et de la construction", "Amphi A"},
                {"Sciences et techniques Licence CPGE", "Amphi B"},
                {"Etudes et metiers de l'ingenieur", "Amphi C"},
                {"Etudes et metiers du management, economie, gestion (BTS/BUT)", "Amphi D"},
                {"Etudes et metiers du secteur social", "Amphi E"},
                {"Etudes et metiers du sport", "Amphi A"},
                {"Lettres et langues Licences CPGE", "Amphi B"},
                {"Sciences de la vie, de l'environnement et de l'agronomie BTS BUT", "Amphi C"},
                {"Etre etudiant / Parcoursup", "Amphi D"}
        };
        for (String[] c : confs) save(repo, "Conference : " + c[0], TypeActivite.CONFERENCE, 80, c[1]);

        String[][] trs = {
                {"Table ronde : Etre etudiant en BTS", "Salle TD1"},
                {"Table ronde : Etre etudiant en BUT", "Salle TD2"},
                {"Table ronde : Etre etudiant en prepa CPI ou CPGE", "Salle TD3"},
                {"Table ronde : Etre alternant dans l'enseignement superieur", "Salle TD4"},
                {"Table ronde : Etre etudiant en Licences", "Salle TD5"}
        };
        for (String[] t : trs) save(repo, t[0], TypeActivite.TABLE_RONDE, 40, t[1]);

        String[][] fms = {
                {"Flash metier : Ingenieur", "Salle FM1"}, {"Flash metier : Social", "Salle FM2"},
                {"Flash metier : Commerce", "Salle FM3"}, {"Flash metier : Sport", "Salle FM4"},
                {"Flash metier : Paramedical", "Salle FM5"}, {"Flash metier : Design / Architecture", "Salle FM6"}
        };
        for (String[] f : fms) save(repo, f[0], TypeActivite.FLASH_METIER, 30, f[1]);
    }

    private void save(ActiviteRepository repo, String titre, TypeActivite type, int places, String salle) {
        Activite a = new Activite();
        a.setTitre(titre);
        a.setType(type);
        a.setNbPlaces(places);
        a.setSalle(salle);
        repo.save(a);
    }
}
