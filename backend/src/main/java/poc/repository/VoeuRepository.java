package poc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import poc.model.Voeu;

import java.util.List;

@Repository
public interface VoeuRepository extends JpaRepository<Voeu, Long> {
    List<Voeu> findByEtudiantId(Long etudiantId);

    @Query("SELECT DISTINCT v.etudiant.id FROM Voeu v")
    List<Long> findEtudiantsWithVoeux();

    @Query("SELECT v FROM Voeu v WHERE v.etudiant.lycee.id = :lyceeId")
    List<Voeu> findByEtudiantLyceeId(Long lyceeId);
}
