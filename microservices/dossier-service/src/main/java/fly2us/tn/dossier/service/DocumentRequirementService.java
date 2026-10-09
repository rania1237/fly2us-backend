package fly2us.tn.dossier.service;

import fly2us.tn.dossier.model.DocumentRequirement;
import fly2us.tn.dossier.repository.DocumentRequirementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentRequirementService {

    private final DocumentRequirementRepository repository;

    public List<DocumentRequirement> getAll() {
        return repository.findAll();
    }

    public List<DocumentRequirement> getByVisaType(String visaType) {
        return repository.findByVisaType(visaType);
    }

    public DocumentRequirement getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document non trouvé"));
    }

    public DocumentRequirement create(DocumentRequirement req) {
        return repository.save(req);
    }

    public DocumentRequirement update(Long id, DocumentRequirement details) {
        DocumentRequirement req = getById(id);
        req.setVisaType(details.getVisaType());
        req.setDocumentName(details.getDocumentName());
        req.setDocumentCode(details.getDocumentCode());
        req.setRequired(details.isRequired());
        req.setDescription(details.getDescription());
        req.setAcceptedFormats(details.getAcceptedFormats());
        req.setMaxSizeMb(details.getMaxSizeMb());
        return repository.save(req);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}