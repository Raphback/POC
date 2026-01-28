package poc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import poc.model.Affectation;

import java.util.List;

@Repository
public interface AffectationRepository extends JpaRepository<Affectation, Long> {
    List<Affectation> findByEtudiantId(Long etudiantId);
    List<Affectation> findByActiviteId(Long activiteId);
    long countByActiviteId(Long activiteId);

    @Query("SELECT COUNT(DISTINCT a.etudiant.id) FROM Affectation a")
    long countDistinctEtudiants();
}
