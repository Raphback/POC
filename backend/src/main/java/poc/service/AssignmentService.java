package poc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poc.model.Activite;
import poc.model.Affectation;
import poc.model.Etudiant;
import poc.model.Voeu;
import poc.repository.ActiviteRepository;
import poc.repository.AffectationRepository;
import poc.repository.EtudiantRepository;
import poc.repository.VoeuRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AssignmentService {

    @Autowired
    private EtudiantRepository etudiantRepository;

    @Autowired
    private ActiviteRepository activiteRepository;

    @Autowired
    private VoeuRepository voeuRepository;

    @Autowired
    private AffectationRepository affectationRepository;

    @Transactional
    public String runAssignment() {
        // 1. Clear previous assignments
        affectationRepository.deleteAll();

        // 2. Load data
        List<Etudiant> etudiants = etudiantRepository.findAll();
        List<Activite> activites = activiteRepository.findAll();
        List<Voeu> allVoeux = voeuRepository.findAll();

        // Map Activities by ID for quick access
        Map<Long, Activite> activiteMap = activites.stream()
                .collect(Collectors.toMap(Activite::getId, a -> a));

        // Track current capacity usage
        Map<Long, Integer> currentCapacity = new HashMap<>();
        activites.forEach(a -> currentCapacity.put(a.getId(), 0));

        // Group Voeux by Etudiant
        Map<Long, List<Voeu>> voeuxByEtudiant = allVoeux.stream()
                .collect(Collectors.groupingBy(v -> v.getEtudiant().getId()));

        // Shuffle students for fairness
        Collections.shuffle(etudiants);

        int assignedCount = 0;

        // 3. Pass 1: Voeu 1 (Priority 1)
        for (Etudiant etudiant : etudiants) {
            assignVoeu(etudiant, 1, voeuxByEtudiant, activiteMap, currentCapacity);
        }

        // 4. Pass 2: Voeu 2 (Priority 2)
        for (Etudiant etudiant : etudiants) {
            assignVoeu(etudiant, 2, voeuxByEtudiant, activiteMap, currentCapacity);
        }

        // 5. Pass 3: Voeu 3, 4, 5 (Options)
        for (int priority = 3; priority <= 5; priority++) {
            for (Etudiant etudiant : etudiants) {
                assignVoeu(etudiant, priority, voeuxByEtudiant, activiteMap, currentCapacity);
            }
        }

        return "Affectation terminée.";
    }

    public List<Affectation> getAllAffectations() {
        return affectationRepository.findAll();
    }

    private void assignVoeu(Etudiant etudiant, int priority,
                            Map<Long, List<Voeu>> voeuxByEtudiant,
                            Map<Long, Activite> activiteMap,
                            Map<Long, Integer> currentCapacity) {

        List<Voeu> studentVoeux = voeuxByEtudiant.getOrDefault(etudiant.getId(), Collections.emptyList());
        
        // Find the wish with the specific priority
        Optional<Voeu> targetVoeu = studentVoeux.stream()
                .filter(v -> v.getPriorite() == priority)
                .findFirst();

        if (targetVoeu.isPresent()) {
            Activite activite = activiteMap.get(targetVoeu.get().getActivite().getId());
            
            // Check capacity (Assuming 'nbPlaces' is the capacity field in Activite, need to verify)
            // If 'nbPlaces' is null, assume unlimited or handle error. 
            // For now, let's assume a default if null, or check the entity.
            // Checking Activite entity... assuming 'nbPlaces' exists.
            
            int capacity = (activite.getNbPlaces() != null) ? activite.getNbPlaces() : 999;
            int current = currentCapacity.getOrDefault(activite.getId(), 0);

            if (current < capacity) {
                // Assign
                Affectation affectation = new Affectation();
                affectation.setEtudiant(etudiant);
                affectation.setActivite(activite);
                affectation.setRangVoeu(priority);
                affectationRepository.save(affectation);

                // Update capacity
                currentCapacity.put(activite.getId(), current + 1);
            } else {
                // Capacity full - Student misses this wish
                // TODO: Handle fallback (waiting list or random assignment)
                System.out.println("Etudiant " + etudiant.getNom() + " n'a pas eu son voeu " + priority + " (Complet)");
            }
        }
    }
}
