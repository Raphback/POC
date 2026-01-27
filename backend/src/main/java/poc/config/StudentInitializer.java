package poc.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import poc.repository.EtudiantRepository;
import poc.repository.LyceeRepository;
import poc.repository.ViewerRepository;
import poc.service.ExcelService;

import java.io.File;

@Configuration
public class StudentInitializer {

    @Bean
    @Order(2)
    public CommandLineRunner initStudents(EtudiantRepository etudiantRepository, LyceeRepository lyceeRepository,
            poc.repository.VoeuRepository voeuRepository, poc.repository.AffectationRepository affectationRepository,
            poc.repository.ActiviteRepository activiteRepository,
            ViewerRepository viewerRepository,
            ExcelService excelService) {
        return args -> {
            File inputsFolder = new File("Inputs");
            boolean hasExcelFiles = inputsFolder.exists() && inputsFolder.isDirectory()
                    && inputsFolder.listFiles((dir, name) -> name.endsWith(".xlsx")) != null
                    && inputsFolder.listFiles((dir, name) -> name.endsWith(".xlsx")).length > 0;

            if (hasExcelFiles) {
                affectationRepository.deleteAll();
                voeuRepository.deleteAll();
                etudiantRepository.deleteAll();
                activiteRepository.deleteAll();
                viewerRepository.deleteAll();

                excelService.importActivities("Inputs", activiteRepository);
                excelService.importStudents("Inputs", etudiantRepository, lyceeRepository);
                excelService.importViewers("Inputs", viewerRepository, lyceeRepository);
            } else {
                System.out.println("📂 No Excel files in Inputs folder - keeping test data from DataInitializer");
            }

            System.out.println("📊 Total Students: " + etudiantRepository.count());
            System.out.println("👁️ Total Viewers: " + viewerRepository.count());
        };
    }
}
