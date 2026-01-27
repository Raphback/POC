package poc.model;

import jakarta.persistence.*;

@Entity
public class Affectation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "etudiant_id")
    private Etudiant etudiant;

    @ManyToOne
    @JoinColumn(name = "activite_id")
    private Activite activite;

    private Integer rangVoeu; // 1, 2, 3, 4, 5 (or null if forced assignment)

    public Affectation() {
    }

    public Affectation(Long id, Etudiant etudiant, Activite activite, Integer rangVoeu) {
        this.id = id;
        this.etudiant = etudiant;
        this.activite = activite;
        this.rangVoeu = rangVoeu;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Etudiant getEtudiant() {
        return etudiant;
    }

    public void setEtudiant(Etudiant etudiant) {
        this.etudiant = etudiant;
    }

    public Activite getActivite() {
        return activite;
    }

    public void setActivite(Activite activite) {
        this.activite = activite;
    }

    public Integer getRangVoeu() {
        return rangVoeu;
    }

    public void setRangVoeu(Integer rangVoeu) {
        this.rangVoeu = rangVoeu;
    }
}
