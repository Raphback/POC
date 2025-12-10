package poc.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import poc.repository.EtudiantRepository;
import poc.repository.LyceeRepository;
import poc.service.ExcelService;

@Configuration
public class StudentInitializer {

    @Bean
    public CommandLineRunner initStudents(EtudiantRepository etudiantRepository, LyceeRepository lyceeRepository, 
                                          poc.repository.VoeuRepository voeuRepository, poc.repository.AffectationRepository affectationRepository,
                                          poc.repository.ActiviteRepository activiteRepository,
                                          ExcelService excelService) {
        return args -> {
            System.out.println("🧹 Cleaning existing data...");
            affectationRepository.deleteAll();
            voeuRepository.deleteAll();
            etudiantRepository.deleteAll();
            activiteRepository.deleteAll(); // Clean activities too
            
            System.out.println("🚀 Starting Import from Excel...");
            excelService.importActivities("/Inputs", activiteRepository);
            excelService.importStudents("/Inputs", etudiantRepository, lyceeRepository);
            
            System.out.println("✅ Student Import Completed.");
            System.out.println("📊 Total Students: " + etudiantRepository.count());
        };
    }
}
