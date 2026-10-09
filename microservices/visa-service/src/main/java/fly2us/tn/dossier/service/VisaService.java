package fly2us.tn.dossier.service;  // ✅ Package correct

import fly2us.tn.dossier.model.Visa;
import fly2us.tn.dossier.model.VisaType;
import fly2us.tn.dossier.repository.VisaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VisaService {

    private final VisaRepository visaRepository;

    public List<Visa> getAllVisas() {
        return visaRepository.findAll();
    }

    public Visa getVisaById(Long id) {
        return visaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Visa non trouvé"));
    }

    public List<Visa> getVisasByType(String type) {
        VisaType visaType = VisaType.valueOf(type.toUpperCase());
        return visaRepository.findByType(visaType);
    }

    public Visa createVisa(Visa visa) {
        if (visaRepository.existsByCountry(visa.getCountry())) {
            throw new RuntimeException("Ce pays existe déjà");
        }
        return visaRepository.save(visa);
    }

    public Visa updateVisa(Long id, Visa visaDetails) {
        Visa visa = getVisaById(id);
        visa.setType(visaDetails.getType());
        visa.setCountry(visaDetails.getCountry());
        visa.setFlag(visaDetails.getFlag());
        visa.setImage(visaDetails.getImage());
        visa.setDescription(visaDetails.getDescription());
        visa.setRequirements(visaDetails.getRequirements());
        visa.setDuration(visaDetails.getDuration());
        visa.setProcessingTime(visaDetails.getProcessingTime());
        visa.setPrice(visaDetails.getPrice());
        return visaRepository.save(visa);
    }

    public void deleteVisa(Long id) {
        visaRepository.deleteById(id);
    }
}