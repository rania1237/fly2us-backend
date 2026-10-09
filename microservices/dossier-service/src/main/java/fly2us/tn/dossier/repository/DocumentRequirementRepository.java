package fly2us.tn.dossier.repository;

import fly2us.tn.dossier.model.DocumentRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRequirementRepository extends JpaRepository<DocumentRequirement, Long> {
    List<DocumentRequirement> findByVisaType(String visaType);
    List<DocumentRequirement> findByVisaTypeAndRequired(String visaType, boolean required);
}