package poc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poc.model.Activite;
import poc.model.Etudiant;
import poc.model.TypeActivite;
import poc.model.Voeu;
import poc.repository.ActiviteRepository;
import poc.repository.EtudiantRepository;
import poc.repository.VoeuRepository;

import java.util.List;

@Service
public class VoeuService {

    @Autowired
    private VoeuRepository voeuRepository;

    @Autowired
    private EtudiantRepository etudiantRepository;

    @Autowired
    private ActiviteRepository activiteRepository;

    public void enregistrerVoeux(Long etudiantId, List<Long> activitesIds) {
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));

        if (activitesIds.size() != 5) {
            throw new RuntimeException("Il faut exactement 5 vœux.");
        }

        List<Activite> activites = activiteRepository.findAllById(activitesIds);
        if (activites.size() != 5) {
            throw new RuntimeException("Certaines activités n'existent pas.");
        }

        // Vérification des règles (3-4-5)
        // Voeu 1 & 2 doivent être des CONFÉRENCES
        if (activites.get(0).getType() != TypeActivite.CONFERENCE || activites.get(1).getType() != TypeActivite.CONFERENCE) {
            throw new RuntimeException("Les vœux 1 et 2 doivent être des CONFÉRENCES.");
        }

        // Vérification des choix 3, 4, 5
        long nbConf = activites.subList(2, 5).stream().filter(a -> a.getType() == TypeActivite.CONFERENCE).count();
        long nbTable = activites.subList(2, 5).stream().filter(a -> a.getType() == TypeActivite.TABLE_RONDE).count();
        long nbFlash = activites.subList(2, 5).stream().filter(a -> a.getType() == TypeActivite.FLASH_METIER).count();

        boolean valid = (nbConf == 3) ||
                        (nbConf == 2 && nbFlash == 1) ||
                        (nbConf == 2 && nbTable == 1) ||
                        (nbConf == 1 && nbTable == 1 && nbFlash == 1);

        if (!valid) {
            throw new RuntimeException("La combinaison des vœux 3, 4 et 5 est invalide (Règle 3-4-5).");
        }

        // Sauvegarde
        // On supprime les anciens vœux pour cet étudiant (mode remplacement)
        List<Voeu> anciensVoeux = voeuRepository.findByEtudiantId(etudiantId);
        voeuRepository.deleteAll(anciensVoeux);

        for (int i = 0; i < 5; i++) {
            Voeu voeu = new Voeu();
            voeu.setEtudiant(etudiant);
            voeu.setActivite(activites.get(i));
            voeu.setPriorite(i + 1);
            voeuRepository.save(voeu);
        }
    }
}
