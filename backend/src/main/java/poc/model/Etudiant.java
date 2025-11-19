package poc.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "etudiant")
public class Etudiant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // L'ID présent dans le fichier CSV (ex: matricule scolaire)
    @Column(name = "matricule_csv", unique = true, nullable = false)
    private String matriculeCsv;

    private String nom;
    private String prenom;
    
    // Bac Général ou Techno (Important pour les stats)
    @Column(name = "serie_bac")
    private String serieBac; 
    
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "lycee_id")
    private Lycee lycee;

    private String classe;
    
    // Sur quelle demi-journée l'élève est présent (1 à 4)
    @Column(name = "demi_journee")
    private Integer demiJournee;
}
