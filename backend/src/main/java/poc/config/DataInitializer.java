package poc.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

import poc.model.Activite;
import poc.model.Admin;
import poc.model.Lycee;
import poc.model.TypeActivite;
import poc.model.Viewer;
import poc.repository.ActiviteRepository;
import poc.repository.AdminRepository;
import poc.repository.EtudiantRepository;
import poc.repository.LyceeRepository;
import poc.repository.ViewerRepository;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class DataInitializer {

        @Bean
        CommandLineRunner initDatabase(ActiviteRepository activiteRepository,
                        LyceeRepository lyceeRepository,
                        AdminRepository adminRepository,
                        ViewerRepository viewerRepository,
                        EtudiantRepository etudiantRepository,
                        JdbcTemplate jdbcTemplate) {
                return args -> {
                        initAdmin(adminRepository);
                        Lycee lyceeFauriel = initLycee(lyceeRepository, "Fauriel", "Lycée Claude Fauriel");
                        Lycee lyceeBrassens = initLycee(lyceeRepository, "Brassens", "Lycée Georges Brassens");
                        initViewers(viewerRepository, lyceeFauriel, lyceeBrassens);
                        cleanDuplicateINE(jdbcTemplate);
                        importStudents(jdbcTemplate, etudiantRepository);
                        initActivites(activiteRepository);
                        printCredentials();
                };
        }

        private void initAdmin(AdminRepository adminRepository) {
                if (adminRepository.findByUsername("admin").isEmpty()) {
                        Admin admin = new Admin("admin", "admin", Admin.Role.SUPER_ADMIN);
                        adminRepository.save(admin);
                }
        }

        private Lycee initLycee(LyceeRepository lyceeRepository, String searchName, String fullName) {
                Lycee lycee = lyceeRepository.findAll().stream()
                                .filter(l -> l.getNom().contains(searchName))
                                .findFirst()
                                .orElse(null);

                if (lycee == null) {
                        lycee = new Lycee();
                        lycee.setNom(fullName);
                        lycee = lyceeRepository.save(lycee);
                }
                return lycee;
        }

        private void initViewers(ViewerRepository viewerRepository, Lycee lyceeFauriel, Lycee lyceeBrassens) {
                if (viewerRepository.findByEmail("prof@fauriel.fr").isEmpty()) {
                        viewerRepository.save(new Viewer("prof@fauriel.fr", "prof", "Dupont", "Jean", lyceeFauriel));
                }
                if (viewerRepository.findByEmail("prof@brassens.fr").isEmpty()) {
                        viewerRepository.save(new Viewer("prof@brassens.fr", "prof", "Martin", "Marie", lyceeBrassens));
                }
        }

        private void cleanDuplicateINE(JdbcTemplate jdbcTemplate) {
                try {
                        String findDuplicatesQuery = """
                                            SELECT ine FROM etudiant
                                            WHERE ine IS NOT NULL
                                            GROUP BY ine
                                            HAVING COUNT(*) > 1
                                        """;
                        List<String> duplicateInes = jdbcTemplate.queryForList(findDuplicatesQuery, String.class);

                        for (String ine : duplicateInes) {
                                String deleteQuery = """
                                                    DELETE FROM etudiant
                                                    WHERE ine = ?
                                                    AND id NOT IN (SELECT MIN(id) FROM etudiant WHERE ine = ?)
                                                """;
                                jdbcTemplate.update(deleteQuery, ine, ine);
                        }
                } catch (Exception ignored) {
                }
        }

        private void importStudents(JdbcTemplate jdbcTemplate, EtudiantRepository etudiantRepository) {
                try {
                        ClassPathResource resource = new ClassPathResource("data.sql");
                        if (resource.exists()) {
                                String sql = new String(resource.getInputStream().readAllBytes(),
                                                StandardCharsets.UTF_8);
                                String[] statements = sql.split(";");
                                for (String stmt : statements) {
                                        stmt = stmt.trim();
                                        if (!stmt.isEmpty() && !stmt.startsWith("--")) {
                                                try {
                                                        jdbcTemplate.execute(stmt);
                                                } catch (Exception ignored) {
                                                }
                                        }
                                }
                        }
                } catch (Exception ignored) {
                }
        }

        private void initActivites(ActiviteRepository activiteRepository) {
                if (activiteRepository.count() > 0)
                        return;

                activiteRepository.save(
                                createActivite("Conférence : Etudes et métiers des arts, de la culture et du design",
                                                TypeActivite.CONFERENCE, 80, "Amphi A"));
                activiteRepository.save(createActivite("Conférence : Etudes et métiers du commerce",
                                TypeActivite.CONFERENCE, 80, "Amphi B"));
                activiteRepository.save(createActivite("Conférence : Les études médicales", TypeActivite.CONFERENCE, 80,
                                "Amphi C"));
                activiteRepository.save(createActivite("Conférence : Management économie gestion Licences CPGE",
                                TypeActivite.CONFERENCE, 80, "Amphi D"));
                activiteRepository.save(createActivite("Conférence : Sciences et innovations technologiques BTS BUT",
                                TypeActivite.CONFERENCE, 80, "Amphi E"));
                activiteRepository.save(createActivite(
                                "Conférence : Sociologie, sciences de l'éducation, histoire géographie Licences CPGE",
                                TypeActivite.CONFERENCE, 80, "Amphi A"));
                activiteRepository
                                .save(createActivite("Conférence : Etudes et métiers de l'informatique et du numérique",
                                                TypeActivite.CONFERENCE, 80, "Amphi B"));
                activiteRepository.save(createActivite("Conférence : Etudes et métiers du droit - Science Po",
                                TypeActivite.CONFERENCE, 80, "Amphi C"));
                activiteRepository.save(createActivite("Conférence : Etudes et métiers du soin et de la santé",
                                TypeActivite.CONFERENCE, 80, "Amphi D"));
                activiteRepository.save(
                                createActivite("Conférence : Etudes et métiers du tourisme et de l'hôtellerie BTS",
                                                TypeActivite.CONFERENCE, 80, "Amphi E"));
                activiteRepository.save(
                                createActivite("Conférence : Etudes et métiers de l'habitat et de la construction",
                                                TypeActivite.CONFERENCE, 80, "Amphi A"));
                activiteRepository.save(createActivite("Conférence : Sciences et techniques Licence CPGE",
                                TypeActivite.CONFERENCE, 80, "Amphi B"));
                activiteRepository.save(createActivite("Conférence : Etudes et métiers de l'ingénieur",
                                TypeActivite.CONFERENCE, 80, "Amphi C"));
                activiteRepository.save(createActivite(
                                "Conférence : Etudes et métiers du management, économie, gestion (BTS/BUT)",
                                TypeActivite.CONFERENCE, 80, "Amphi D"));
                activiteRepository.save(createActivite("Conférence : Etudes et métiers du secteur social",
                                TypeActivite.CONFERENCE, 80, "Amphi E"));
                activiteRepository.save(createActivite("Conférence : Etudes et métiers du sport",
                                TypeActivite.CONFERENCE, 80, "Amphi A"));
                activiteRepository.save(createActivite("Conférence : Lettres et langues Licences CPGE",
                                TypeActivite.CONFERENCE, 80, "Amphi B"));
                activiteRepository.save(createActivite(
                                "Conférence : Sciences de la vie, de l'environnement et de l'agronomie BTS BUT",
                                TypeActivite.CONFERENCE, 80, "Amphi C"));
                activiteRepository.save(createActivite("Conférence : Etre étudiant / Parcoursup",
                                TypeActivite.CONFERENCE, 80, "Amphi D"));

                activiteRepository
                                .save(createActivite("Table ronde : Etre étudiant en BTS (animation par des étudiants)",
                                                TypeActivite.TABLE_RONDE, 40, "Salle TD1"));
                activiteRepository
                                .save(createActivite("Table ronde : Etre étudiant en BUT (animation par des étudiants)",
                                                TypeActivite.TABLE_RONDE, 40, "Salle TD2"));
                activiteRepository.save(createActivite(
                                "Table ronde : Etre étudiant en prépa CPI ou CPGE (animation par des étudiants)",
                                TypeActivite.TABLE_RONDE, 40, "Salle TD3"));
                activiteRepository.save(createActivite(
                                "Table ronde : Etre alternant dans l'enseignement supérieur (animation par des étudiants)",
                                TypeActivite.TABLE_RONDE, 40, "Salle TD4"));
                activiteRepository.save(
                                createActivite("Table ronde : Etre étudiant en Licences (animation par des étudiants)",
                                                TypeActivite.TABLE_RONDE, 40, "Salle TD5"));

                activiteRepository.save(createActivite("Flash métier : Ingénieur (animation par des professionnels)",
                                TypeActivite.FLASH_METIER, 30, "Salle FM1"));
                activiteRepository.save(createActivite("Flash métier : Social (animation par des professionnels)",
                                TypeActivite.FLASH_METIER, 30, "Salle FM2"));
                activiteRepository.save(createActivite("Flash métier : Commerce (animation par des professionnels)",
                                TypeActivite.FLASH_METIER, 30, "Salle FM3"));
                activiteRepository.save(createActivite("Flash métier : Sport (animation par des professionnels)",
                                TypeActivite.FLASH_METIER, 30, "Salle FM4"));
                activiteRepository.save(createActivite("Flash métier : Paramédical (animation par des professionnels)",
                                TypeActivite.FLASH_METIER, 30, "Salle FM5"));
                activiteRepository.save(createActivite(
                                "Flash métier : Design / Architecture (animation par des professionnels)",
                                TypeActivite.FLASH_METIER, 30, "Salle FM6"));
        }

        private void printCredentials() {
                System.out.println("===========================================");
                System.out.println("IDENTIFIANTS DE TEST:");
                System.out.println("  ADMIN: admin / admin");
                System.out.println("  VIEWER: prof@fauriel.fr / prof");
                System.out.println("  VIEWER: prof@brassens.fr / prof");
                System.out.println("  ETUDIANT: INE (ex: 120890177FA)");
                System.out.println("===========================================");
        }

        private Activite createActivite(String titre, TypeActivite type, int places, String salle) {
                Activite activite = new Activite();
                activite.setTitre(titre);
                activite.setType(type);
                activite.setNbPlaces(places);
                activite.setSalle(salle);
                return activite;
        }
}
