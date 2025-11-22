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
    private VoeuRepository voeuRepository;

    @Autowired
    private ActiviteRepository activiteRepository;

    @Autowired
    private AffectationRepository affectationRepository;

    public Map<String, Object> getGlobalStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Total counts
        long totalEtudiants = etudiantRepository.count();
        long totalActivites = activiteRepository.count();
        long totalVoeux = voeuRepository.count();
        long totalAffectations = affectationRepository.count();

        stats.put("totalEtudiants", totalEtudiants);
        stats.put("totalActivites", totalActivites);
        stats.put("totalVoeux", totalVoeux);
        stats.put("totalAffectations", totalAffectations);

        // Statistics by lycee
        List<Etudiant> etudiants = etudiantRepository.findAll();
        Map<String, Long> etudiantsParLycee = etudiants.stream()
                .filter(e -> e.getLycee() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getLycee().getNom(),
                        Collectors.counting()
                ));
        stats.put("etudiantsParLycee", etudiantsParLycee);

        // Statistics by serie bac
        Map<String, Long> etudiantsParSerie = etudiants.stream()
                .filter(e -> e.getSerieBac() != null)
                .collect(Collectors.groupingBy(
                        Etudiant::getSerieBac,
                        Collectors.counting()
                ));
        stats.put("etudiantsParSerie", etudiantsParSerie);

        // Statistics by demi-journee
        Map<String, Long> etudiantsParDemiJournee = etudiants.stream()
                .filter(e -> e.getDemiJournee() != null)
                .collect(Collectors.groupingBy(
                        Etudiant::getDemiJournee,
                        Collectors.counting()
                ));
        stats.put("etudiantsParDemiJournee", etudiantsParDemiJournee);

        // Activity fill rates
        List<Activite> activites = activiteRepository.findAll();
        Map<String, Map<String, Object>> tauxRemplissage = new HashMap<>();
        
        for (Activite activite : activites) {
            long affectes = affectationRepository.findAll().stream()
                    .filter(a -> a.getActivite().getId().equals(activite.getId()))
                    .count();
            
            Map<String, Object> activityStats = new HashMap<>();
            activityStats.put("capacite", activite.getNbPlaces());
            activityStats.put("affectes", affectes);
            activityStats.put("tauxRemplissage", activite.getNbPlaces() > 0 
                    ? (double) affectes / activite.getNbPlaces() * 100 
                    : 0);
            
            tauxRemplissage.put(activite.getTitre(), activityStats);
        }
        stats.put("tauxRemplissageActivites", tauxRemplissage);

        // Satisfaction rate (students who got at least one wish)
        long etudiantsAvecAffectation = affectationRepository.findAll().stream()
                .map(a -> a.getEtudiant().getId())
                .distinct()
                .count();
        
        double tauxSatisfaction = totalEtudiants > 0 
                ? (double) etudiantsAvecAffectation / totalEtudiants * 100 
                : 0;
        stats.put("tauxSatisfaction", tauxSatisfaction);

        return stats;
    }

    public Map<String, Object> getStatisticsByLycee(Long lyceeId) {
        Map<String, Object> stats = new HashMap<>();
        
        List<Etudiant> etudiants = etudiantRepository.findAll().stream()
                .filter(e -> e.getLycee() != null && e.getLycee().getId().equals(lyceeId))
                .collect(Collectors.toList());
        
        stats.put("totalEtudiants", etudiants.size());
        
        // Voeux count
        long totalVoeux = etudiants.stream()
                .mapToLong(e -> voeuRepository.findAll().stream()
                        .filter(v -> v.getEtudiant().getId().equals(e.getId()))
                        .count())
                .sum();
        stats.put("totalVoeux", totalVoeux);
        
        // Affectations count
        long totalAffectations = etudiants.stream()
                .mapToLong(e -> affectationRepository.findAll().stream()
                        .filter(a -> a.getEtudiant().getId().equals(e.getId()))
                        .count())
                .sum();
        stats.put("totalAffectations", totalAffectations);
        
        return stats;
    }
}
