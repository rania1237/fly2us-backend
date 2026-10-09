package fly2us.tn.dossier.service;

import fly2us.tn.dossier.model.Dossier;
import fly2us.tn.dossier.model.DossierDocument;
import fly2us.tn.dossier.model.DocumentRequirement;
import fly2us.tn.dossier.repository.DossierDocumentRepository;
import fly2us.tn.dossier.repository.DossierRepository;
import fly2us.tn.dossier.repository.DocumentRequirementRepository;
import fly2us.tn.dossier.client.NotificationClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DossierService {

    private final DossierRepository dossierRepository;
    private final DossierDocumentRepository documentRepository;
    private final DocumentRequirementRepository requirementRepository;
    private final FileStorageService fileStorageService;
    private final NotificationClient notificationClient;

    // ==========================================
    // ✅ CRÉER UN DOSSIER (numéro unique garanti)
    // ==========================================
    @Transactional
    public Dossier createDossier(Dossier dossier) {
        String year = String.valueOf(LocalDateTime.now().getYear());

        // ✅ Utiliser le timestamp pour garantir un numéro unique
        String uniqueId = String.valueOf(System.currentTimeMillis());
        String count = uniqueId.substring(uniqueId.length() - 6);  // 6 derniers chiffres

        dossier.setDossierNumber("DOS-" + year + "-" + count);
        dossier.setStatus("BROUILLON");
        return dossierRepository.save(dossier);
    }

    // ==========================================
    // UPLOADER UN DOCUMENT
    // ==========================================
    @Transactional
    public DossierDocument uploadDocument(Long dossierId, Long requirementId, MultipartFile file, Long userId) {
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'id : " + dossierId));

        DocumentRequirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new RuntimeException("Document requis non trouvé avec l'id : " + requirementId));

        String filePath = fileStorageService.store(file);

        Optional<DossierDocument> existing = documentRepository
                .findByDossier_IdAndRequirementId(dossierId, requirementId);

        DossierDocument document = existing.orElse(new DossierDocument());

        document.setDossier(dossier);
        document.setRequirementId(requirementId);
        document.setDocumentType(requirement.getDocumentCode());
        document.setFileName(file.getOriginalFilename());
        document.setFilePath(filePath);
        document.setFileSize(file.getSize());
        document.setFileType(file.getContentType());
        document.setStatus("UPLOADED");
        document.setUploadedBy(userId);
        document.setUploadedAt(LocalDateTime.now());

        if (existing.isPresent()) {
            document.setValidatedBy(null);
            document.setValidatedAt(null);
            document.setRejectionReason(null);
        }

        DossierDocument saved = documentRepository.save(document);

        if ("DOCUMENT_REJETE".equals(dossier.getStatus())) {
            List<DossierDocument> allDocs = documentRepository.findByDossier_Id(dossierId);
            boolean hasRejected = allDocs.stream().anyMatch(d -> "REJECTED".equals(d.getStatus()));

            if (!hasRejected) {
                dossier.setStatus("EN_ATTENTE_VERIFICATION");
                dossier.setRejectionReason(null);
                dossierRepository.save(dossier);
                System.out.println("✅ Dossier " + dossierId + " repassé en EN_ATTENTE_VERIFICATION");
            }
        }

        return saved;
    }

    // ==========================================
    // VÉRIFIER SI LE DOSSIER EST COMPLET
    // ==========================================
    public boolean isDossierComplete(Long dossierId) {
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'id : " + dossierId));

        List<DocumentRequirement> requiredDocs = requirementRepository
                .findByVisaTypeAndRequired(dossier.getType(), true);

        if (requiredDocs.isEmpty()) {
            return true;
        }

        List<DossierDocument> uploadedDocs = documentRepository.findByDossier_Id(dossierId);

        for (DocumentRequirement req : requiredDocs) {
            boolean found = uploadedDocs.stream()
                    .anyMatch(doc -> doc.getRequirementId() != null
                            && doc.getRequirementId().equals(req.getId())
                            && !"REJECTED".equals(doc.getStatus()));
            if (!found) {
                return false;
            }
        }
        return true;
    }

    // ==========================================
    // SOUMETTRE LE DOSSIER
    // ==========================================
    @Transactional
    public Dossier submitDossier(Long dossierId, Long clientId) {
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'id : " + dossierId));

        if (!isDossierComplete(dossierId)) {
            throw new RuntimeException("Le dossier n'est pas complet. Tous les documents obligatoires doivent être fournis.");
        }

        dossier.setStatus("EN_ATTENTE_VERIFICATION");
        dossier.setSubmittedAt(LocalDateTime.now());
        Dossier saved = dossierRepository.save(dossier);

        try {
            notificationClient.sendEmail(
                    clientId,
                    "Dépôt de dossier réussi",
                    "Votre dossier " + dossier.getDossierNumber() + " a bien été déposé et sera vérifié prochainement."
            );
        } catch (Exception e) {
            System.err.println("⚠️ Notification non envoyée : " + e.getMessage());
        }

        return saved;
    }

    // ==========================================
    // VALIDER UN DOCUMENT
    // ==========================================
    @Transactional
    public DossierDocument validateDocument(Long documentId, Long adminId) {
        DossierDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document non trouvé avec l'id : " + documentId));

        document.setStatus("VALIDATED");
        document.setValidatedBy(adminId);
        document.setValidatedAt(LocalDateTime.now());

        return documentRepository.save(document);
    }

    // ==========================================
    // REJETER UN DOCUMENT
    // ==========================================
    @Transactional
    public DossierDocument rejectDocument(Long documentId, Long adminId, String reason) {
        DossierDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document non trouvé avec l'id : " + documentId));

        document.setStatus("REJECTED");
        document.setRejectionReason(reason);
        document.setValidatedBy(adminId);
        document.setValidatedAt(LocalDateTime.now());

        DossierDocument saved = documentRepository.save(document);

        try {
            notificationClient.sendAlert(
                    document.getDossier().getClientId(),
                    "Document rejeté",
                    "Le document " + document.getFileName() + " a été rejeté. Raison : " + reason
            );
        } catch (Exception e) {
            System.err.println("⚠️ Alerte non envoyée : " + e.getMessage());
        }

        Dossier dossier = document.getDossier();
        dossier.setStatus("DOCUMENT_REJETE");
        dossier.setRejectionReason("Document " + document.getDocumentType() + " rejeté : " + reason);
        dossierRepository.save(dossier);

        return saved;
    }

    // ==========================================
    // GÉNÉRER LE PDF
    // ==========================================
    public byte[] generateDossierPdf(Long dossierId) {
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'id : " + dossierId));

        List<DossierDocument> documents = documentRepository.findByDossier_Id(dossierId);

        return PdfGenerator.generateDossierPdf(dossier, documents);
    }

    public List<Dossier> getAllDossiers() { return dossierRepository.findAll(); }
    public List<Dossier> getDossiersByClient(Long clientId) { return dossierRepository.findByClientId(clientId); }
    public List<Dossier> getDossiersByAgent(Long agentId) { return dossierRepository.findByAgentId(agentId); }
    public List<Dossier> getDossiersByStatus(String status) { return dossierRepository.findByStatus(status); }

    public Dossier getDossierById(Long id) {
        return dossierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'id : " + id));
    }

    public Dossier getDossierByNumber(String number) {
        return dossierRepository.findByDossierNumber(number)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec le numéro : " + number));
    }

    public List<DossierDocument> getDocumentsByDossier(Long dossierId) {
        return documentRepository.findByDossier_Id(dossierId);
    }

    public DossierDocument getDocumentById(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document non trouvé avec l'id : " + documentId));
    }

    @Transactional
    public void deleteDossier(Long id) {
        if (!dossierRepository.existsById(id)) {
            throw new RuntimeException("Dossier non trouvé avec l'id : " + id);
        }
        dossierRepository.deleteById(id);
    }

    @Transactional
    public Dossier updateDossier(Long id, Dossier details) {
        Dossier dossier = getDossierById(id);
        dossier.setType(details.getType());
        dossier.setCountry(details.getCountry());
        dossier.setPriority(details.getPriority());
        dossier.setUniversity(details.getUniversity());
        dossier.setProgram(details.getProgram());
        dossier.setStartDate(details.getStartDate());
        dossier.setEndDate(details.getEndDate());
        dossier.setNotes(details.getNotes());

        if (details.getStatus() != null) {
            dossier.setStatus(details.getStatus());
        }

        return dossierRepository.save(dossier);
    }
}