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

    @Autowired private EtudiantRepository etudiantRepository;
    @Autowired private ActiviteRepository activiteRepository;
    @Autowired private VoeuRepository voeuRepository;
    @Autowired private AffectationRepository affectationRepository;

    @Transactional
    public String runAssignment() {
        affectationRepository.deleteAll();

        List<Etudiant> etudiants = etudiantRepository.findAll();
        List<Activite> activites = activiteRepository.findAll();

        Map<Long, Activite> activiteMap = activites.stream()
                .collect(Collectors.toMap(Activite::getId, a -> a));

        Map<Long, Integer> currentCapacity = new HashMap<>();
        activites.forEach(a -> currentCapacity.put(a.getId(), 0));

        Map<Long, List<Voeu>> voeuxByEtudiant = voeuRepository.findAll().stream()
                .collect(Collectors.groupingBy(v -> v.getEtudiant().getId()));

        Collections.shuffle(etudiants);

        for (int priority = 1; priority <= 5; priority++) {
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

        studentVoeux.stream()
                .filter(v -> v.getPriorite() == priority)
                .findFirst()
                .ifPresent(voeu -> {
                    Activite activite = activiteMap.get(voeu.getActivite().getId());
                    int capacity = (activite.getNbPlaces() != null) ? activite.getNbPlaces() : 999;
                    int current = currentCapacity.getOrDefault(activite.getId(), 0);

                    if (current < capacity) {
                        Affectation affectation = new Affectation();
                        affectation.setEtudiant(etudiant);
                        affectation.setActivite(activite);
                        affectation.setRangVoeu(priority);
                        affectationRepository.save(affectation);
                        currentCapacity.put(activite.getId(), current + 1);
                    }
                });
    }
}
