package poc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poc.model.Lycee;

import java.util.Optional;

@Repository
public interface LyceeRepository extends JpaRepository<Lycee, Long> {
    Optional<Lycee> findByNom(String nom);
}
