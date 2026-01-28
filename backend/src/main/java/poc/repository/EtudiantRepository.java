package poc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poc.model.Etudiant;

import java.util.List;
import java.util.Optional;

@Repository
public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {
    Optional<Etudiant> findByMatriculeCsv(String matriculeCsv);
    Optional<Etudiant> findByIne(String ine);
    List<Etudiant> findByLyceeId(Long lyceeId);
}
