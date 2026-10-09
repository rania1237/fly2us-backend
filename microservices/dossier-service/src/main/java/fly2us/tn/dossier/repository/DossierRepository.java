package fly2us.tn.dossier.repository;

import fly2us.tn.dossier.model.Dossier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DossierRepository extends JpaRepository<Dossier, Long> {
    Optional<Dossier> findByDossierNumber(String dossierNumber);
    List<Dossier> findByClientId(Long clientId);
    List<Dossier> findByAgentId(Long agentId);
    List<Dossier> findByStatus(String status);
    boolean existsByDossierNumber(String dossierNumber);
}