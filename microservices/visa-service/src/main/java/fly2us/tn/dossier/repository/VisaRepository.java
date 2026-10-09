package fly2us.tn.dossier.repository;  // ✅ Package correct

import fly2us.tn.dossier.model.Visa;
import fly2us.tn.dossier.model.VisaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VisaRepository extends JpaRepository<Visa, Long> {
    List<Visa> findByType(VisaType type);
    Optional<Visa> findByCountry(String country);
    boolean existsByCountry(String country);
}