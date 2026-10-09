package fly2us.tn.payment.repository;

import fly2us.tn.payment.model.VisaTarif;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VisaTarifRepository extends JpaRepository<VisaTarif, Long> {

    List<VisaTarif> findByActiveTrue();

    List<VisaTarif> findByVisaType(String visaType);

    // ✅ Nouvelle recherche : type + priorité (SANS pays)
    Optional<VisaTarif> findByVisaTypeAndPriority(String visaType, String priority);
}