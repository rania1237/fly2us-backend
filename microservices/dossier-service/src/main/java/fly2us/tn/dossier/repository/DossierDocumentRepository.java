package fly2us.tn.dossier.repository;

import fly2us.tn.dossier.model.DossierDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DossierDocumentRepository extends JpaRepository<DossierDocument, Long> {

    // ✅ CORRECT : Utilise "Dossier_Id" pour traverser la relation @ManyToOne
    List<DossierDocument> findByDossier_Id(Long dossierId);

    // ✅ CORRECT : Avec le statut
    List<DossierDocument> findByDossier_IdAndStatus(Long dossierId, String status);

    // ✅ CORRECT : Vérifier si un document spécifique a déjà été uploadé
    boolean existsByDossier_IdAndRequirementId(Long dossierId, Long requirementId);

    // ✅ CORRECT : Trouver un document spécifique
    Optional<DossierDocument> findByDossier_IdAndRequirementId(Long dossierId, Long requirementId);

    // ✅ CORRECT : Compter les documents d'un dossier
    long countByDossier_Id(Long dossierId);
}