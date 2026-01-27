package poc.model;

import jakarta.persistence.*;

@Entity
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

    @Column(nullable = true)
    private String classe;

    // Sur quelle demi-journée l'élève est présent (1 à 4)
    @Column(name = "demi_journee")
    private String demiJournee;

    // INE (Identifiant National Etudiant) - format: 10 chiffres + 1 lettre
    @Column(name = "ine", unique = true, nullable = true)
    private String ine;

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMatriculeCsv() {
        return matriculeCsv;
    }

    public void setMatriculeCsv(String matriculeCsv) {
        this.matriculeCsv = matriculeCsv;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getSerieBac() {
        return serieBac;
    }

    public void setSerieBac(String serieBac) {
        this.serieBac = serieBac;
    }

    public Lycee getLycee() {
        return lycee;
    }

    public void setLycee(Lycee lycee) {
        this.lycee = lycee;
    }

    public String getClasse() {
        return classe;
    }

    public void setClasse(String classe) {
        this.classe = classe;
    }

    public String getDemiJournee() {
        return demiJournee;
    }

    public void setDemiJournee(String demiJournee) {
        this.demiJournee = demiJournee;
    }

    public String getIne() {
        return ine;
    }

    public void setIne(String ine) {
        this.ine = ine;
    }
}
