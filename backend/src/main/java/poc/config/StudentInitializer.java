package poc.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import poc.model.Etudiant;
import poc.repository.EtudiantRepository;

import poc.model.Lycee;
import poc.repository.LyceeRepository;
import java.util.List;

@Configuration
public class StudentInitializer {

    @Bean
    public CommandLineRunner initStudents(EtudiantRepository etudiantRepository, LyceeRepository lyceeRepository) {
        return args -> {
            if (etudiantRepository.count() == 0) {
                // Get first lycee for assignment
                List<Lycee> lycees = lyceeRepository.findAll();
                Lycee lycee = lycees.isEmpty() ? null : lycees.get(0);
                
                // Étudiant 1 : TEST001 - DUPONT
                Etudiant etudiant1 = new Etudiant();
                etudiant1.setMatriculeCsv("TEST001");
                etudiant1.setNom("DUPONT");
                etudiant1.setPrenom("Jean");
                etudiant1.setSerieBac("S");
                etudiant1.setClasse("TS1");
                etudiant1.setDemiJournee("DJ1");
                if (lycee != null) etudiant1.setLycee(lycee);
                etudiantRepository.save(etudiant1);
                
                // Étudiant 2 : TEST002 - MARTIN
                Etudiant etudiant2 = new Etudiant();
                etudiant2.setMatriculeCsv("TEST002");
                etudiant2.setNom("MARTIN");
                etudiant2.setPrenom("Sophie");
                etudiant2.setSerieBac("ES");
                etudiant2.setClasse("TES2");
                etudiant2.setDemiJournee("DJ2");
                if (lycee != null) etudiant2.setLycee(lycee);
                etudiantRepository.save(etudiant2);
                
                // Étudiant 3 : TEST003 - BERNARD
                Etudiant etudiant3 = new Etudiant();
                etudiant3.setMatriculeCsv("TEST003");
                etudiant3.setNom("BERNARD");
                etudiant3.setPrenom("Thomas");
                etudiant3.setSerieBac("L");
                etudiant3.setClasse("TL1");
                etudiant3.setDemiJournee("DJ1");
                if (lycee != null) etudiant3.setLycee(lycee);
                etudiantRepository.save(etudiant3);
                
                // Étudiant 4 : TEST004 - PETIT
                Etudiant etudiant4 = new Etudiant();
                etudiant4.setMatriculeCsv("TEST004");
                etudiant4.setNom("PETIT");
                etudiant4.setPrenom("Marie");
                etudiant4.setSerieBac("S");
                etudiant4.setClasse("TS2");
                etudiant4.setDemiJournee("DJ2");
                if (lycee != null) etudiant4.setLycee(lycee);
                etudiantRepository.save(etudiant4);
                
                System.out.println("✅ 4 étudiants de test créés :");
                System.out.println("   - TEST001 / DUPONT (Série S)");
                System.out.println("   - TEST002 / MARTIN (Série ES)");
                System.out.println("   - TEST003 / BERNARD (Série L)");
                System.out.println("   - TEST004 / PETIT (Série S)");
            }
        };
    }
}
