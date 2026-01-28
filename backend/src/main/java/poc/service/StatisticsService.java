package poc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poc.model.*;
import poc.repository.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    @Autowired
    private EtudiantRepository etudiantRepository;
    @Autowired
    private ActiviteRepository activiteRepository;
    @Autowired
    private VoeuRepository voeuRepository;
    @Autowired
    private AffectationRepository affectationRepository;

    public Map<String, Object> getGlobalStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalEtudiants = etudiantRepository.count();
        stats.put("totalEtudiants", totalEtudiants);
        stats.put("totalActivites", activiteRepository.count());
        stats.put("totalVoeux", voeuRepository.count());
        stats.put("totalAffectations", affectationRepository.count());

        List<Etudiant> etudiants = etudiantRepository.findAll();
        stats.put("etudiantsParLycee", etudiants.stream()
                .filter(e -> e.getLycee() != null)
                .collect(Collectors.groupingBy(e -> e.getLycee().getNom(), Collectors.counting())));
        stats.put("etudiantsParSerie", etudiants.stream()
                .filter(e -> e.getSerieBac() != null)
                .collect(Collectors.groupingBy(Etudiant::getSerieBac, Collectors.counting())));
        stats.put("etudiantsParDemiJournee", etudiants.stream()
                .filter(e -> e.getDemiJournee() != null)
                .collect(Collectors.groupingBy(Etudiant::getDemiJournee, Collectors.counting())));

        // Taux de remplissage par activite
        Map<String, Map<String, Object>> taux = new HashMap<>();
        for (Activite a : activiteRepository.findAll()) {
            long affectes = affectationRepository.countByActiviteId(a.getId());
            taux.put(a.getTitre(), Map.of(
                    "capacite", a.getNbPlaces(),
                    "affectes", affectes,
                    "tauxRemplissage", a.getNbPlaces() > 0 ? (double) affectes / a.getNbPlaces() * 100 : 0));
        }
        stats.put("tauxRemplissageActivites", taux);

        long avecAffectation = affectationRepository.countDistinctEtudiants();
        stats.put("tauxSatisfaction", totalEtudiants > 0 ? (double) avecAffectation / totalEtudiants * 100 : 0);

        return stats;
    }
}
