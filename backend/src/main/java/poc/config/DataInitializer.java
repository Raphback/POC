package poc.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import poc.model.Activite;
import poc.model.TypeActivite;
import poc.repository.ActiviteRepository;

import poc.model.Lycee;
import poc.repository.LyceeRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(ActiviteRepository repository, LyceeRepository lyceeRepository, poc.repository.AdminRepository adminRepository) {
        return args -> {
            System.out.println("🔍 DataInitializer: Checking ActiviteRepository...");
            long count = repository.count();
            System.out.println("🔍 DataInitializer: Count = " + count);
            
            // Initialize Admin
            if (adminRepository.count() == 0) {
                poc.model.Admin admin = new poc.model.Admin("admin", "admin", poc.model.Admin.Role.SUPER_ADMIN);
                adminRepository.save(admin);
                System.out.println("👮 Admin par défaut créé : admin / admin");
            }

            if (count == 0) {
                System.out.println("🚀 DataInitializer: Starting initialization...");
                
                // Lycées
                Lycee lycee = new Lycee();
                lycee.setNom("Lycée Fauriel");
                lyceeRepository.save(lycee);
                System.out.println("🏫 Lycée créé : " + lycee.getNom());

                // Conférences - DISABLED (Imported from Excel now)
                /*
                repository.save(createActivite("Etudes et métiers des arts, de la culture et du design", TypeActivite.CONFERENCE, 30));
                repository.save(createActivite("Sciences et techniques", TypeActivite.CONFERENCE, 30));
                repository.save(createActivite("Licence CPGE", TypeActivite.CONFERENCE, 30));
                repository.save(createActivite("Etudes et métiers de l'ingénieur", TypeActivite.CONFERENCE, 30));
                repository.save(createActivite("Les métiers de la santé", TypeActivite.CONFERENCE, 30));
                repository.save(createActivite("Droit et Sciences Politiques", TypeActivite.CONFERENCE, 30));
                repository.save(createActivite("Economie et Gestion", TypeActivite.CONFERENCE, 30));
                repository.save(createActivite("Lettres et Langues", TypeActivite.CONFERENCE, 30));
                repository.save(createActivite("Sciences Humaines et Sociales", TypeActivite.CONFERENCE, 30));
                repository.save(createActivite("Sport (STAPS)", TypeActivite.CONFERENCE, 30));
                repository.save(createActivite("Enseignement et Education", TypeActivite.CONFERENCE, 30));
                repository.save(createActivite("Communication et Journalisme", TypeActivite.CONFERENCE, 30));
                repository.save(createActivite("Informatique et Numérique", TypeActivite.CONFERENCE, 30));
                repository.save(createActivite("Environnement et Développement Durable", TypeActivite.CONFERENCE, 30));
                repository.save(createActivite("Architecture et Urbanisme", TypeActivite.CONFERENCE, 30));
                repository.save(createActivite("Défense et Sécurité", TypeActivite.CONFERENCE, 30));
                repository.save(createActivite("Commerce et Marketing", TypeActivite.CONFERENCE, 30));
                repository.save(createActivite("Social et Paramédical", TypeActivite.CONFERENCE, 30));
                repository.save(createActivite("Tourisme et Hôtellerie", TypeActivite.CONFERENCE, 30));

                // Flash métiers et Tables rondes
                repository.save(createActivite("Flash métier: Design / Architecture", TypeActivite.FLASH_METIER, 20));
                repository.save(createActivite("Flash métier: Ingénieur", TypeActivite.FLASH_METIER, 20));
                repository.save(createActivite("Flash métier: Santé", TypeActivite.FLASH_METIER, 20));
                repository.save(createActivite("Flash métier: Droit", TypeActivite.FLASH_METIER, 20));
                repository.save(createActivite("Table ronde: Les métiers de demain", TypeActivite.TABLE_RONDE, 25));
                repository.save(createActivite("Table ronde: L'alternance", TypeActivite.TABLE_RONDE, 25));
                repository.save(createActivite("Table ronde: La vie étudiante", TypeActivite.TABLE_RONDE, 25));
                */

                System.out.println("Base de données initialisée avec succès !");
            }
        };
    }

    private Activite createActivite(String titre, TypeActivite type, int places) {
        Activite activite = new Activite();
        activite.setTitre(titre);
        activite.setType(type);
        activite.setNbPlaces(places);
        return activite;
    }
}
