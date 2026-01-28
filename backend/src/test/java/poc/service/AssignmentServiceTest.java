package poc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import poc.model.*;
import poc.repository.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AssignmentServiceTest {

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private EtudiantRepository etudiantRepository;

    @Autowired
    private ActiviteRepository activiteRepository;

    @Autowired
    private VoeuRepository voeuRepository;

    @Autowired
    private AffectationRepository affectationRepository;

    @BeforeEach
    void setUp() {
        // Clear all previous data for a clean test environment
        affectationRepository.deleteAll();
        voeuRepository.deleteAll();
        etudiantRepository.deleteAll();
        activiteRepository.deleteAll();
    }

    @Test
    void testRunAssignment_Basic() {
        // 1. Create Test Activity
        Activite math = new Activite();
        math.setTitre("Mathématiques");
        math.setType(TypeActivite.CONFERENCE);
        math.setNbPlaces(1);
        math = activiteRepository.save(math);

        // 2. Create Test Student
        Etudiant student = new Etudiant();
        student.setNom("Dupont");
        student.setPrenom("Jean");
        student.setMatriculeCsv("M123");
        student = etudiantRepository.save(student);

        // 3. Create Wish
        Voeu voeu = new Voeu();
        voeu.setEtudiant(student);
        voeu.setActivite(math);
        voeu.setPriorite(1);
        voeuRepository.save(voeu);

        // 4. Run Assignment
        String result = assignmentService.runAssignment();

        // 5. Assertions
        assertEquals("Affectation terminée.", result);
        List<Affectation> affectations = affectationRepository.findAll();
        assertEquals(1, affectations.size());
        assertEquals("Dupont", affectations.get(0).getEtudiant().getNom());
        assertEquals("Mathématiques", affectations.get(0).getActivite().getTitre());
    }

    @Test
    void testRunAssignment_CapacityRespected() {
        // 1. Create Test Activity with capacity 1
        Activite limited = new Activite();
        limited.setTitre("Limited Session");
        limited.setNbPlaces(1);
        limited = activiteRepository.save(limited);

        // 2. Create 2 students
        Etudiant s1 = createStudent("S1", "M1");
        Etudiant s2 = createStudent("S2", "M2");

        // 3. Both students want the same activity as priority 1
        createVoeu(s1, limited, 1);
        createVoeu(s2, limited, 1);

        // 4. Run Assignment
        assignmentService.runAssignment();

        // 5. Assertions: only one student should be assigned
        List<Affectation> affectations = affectationRepository.findAll();
        assertEquals(1, affectations.size(), "Only one student should have been assigned due to capacity");
    }

    private Etudiant createStudent(String nom, String matricule) {
        Etudiant s = new Etudiant();
        s.setNom(nom);
        s.setMatriculeCsv(matricule);
        return etudiantRepository.save(s);
    }

    private void createVoeu(Etudiant e, Activite a, int priority) {
        Voeu v = new Voeu();
        v.setEtudiant(e);
        v.setActivite(a);
        v.setPriorite(priority);
        voeuRepository.save(v);
    }
}
