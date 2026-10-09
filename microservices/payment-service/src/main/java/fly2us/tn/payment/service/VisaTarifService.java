package fly2us.tn.payment.service;

import fly2us.tn.payment.model.VisaTarif;
import fly2us.tn.payment.repository.VisaTarifRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VisaTarifService {

    private final VisaTarifRepository repository;

    public List<VisaTarif> getAll() {
        return repository.findAll();
    }

    public List<VisaTarif> getActive() {
        return repository.findByActiveTrue();
    }

    public VisaTarif getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarif non trouvé avec l'id : " + id));
    }

    // ✅ Nouveau : recherche par type + priorité uniquement
    public VisaTarif getTarifFor(String visaType, String priority) {
        Optional<VisaTarif> tarif = repository.findByVisaTypeAndPriority(visaType, priority);

        if (tarif.isEmpty()) {
            throw new RuntimeException(
                    "Aucun tarif défini pour : " + visaType + " - " + priority);
        }
        return tarif.get();
    }

    @Transactional
    public VisaTarif create(VisaTarif tarif) {
        tarif.setTotalAmount(
                (tarif.getServiceFee() != null ? tarif.getServiceFee() : 0.0)
                        + (tarif.getTlsFee() != null ? tarif.getTlsFee() : 0.0)
        );
        return repository.save(tarif);
    }

    @Transactional
    public VisaTarif update(Long id, VisaTarif details) {
        VisaTarif tarif = getById(id);
        tarif.setVisaType(details.getVisaType());
        tarif.setPriority(details.getPriority());
        tarif.setServiceFee(details.getServiceFee());
        tarif.setTlsFee(details.getTlsFee());
        tarif.setTotalAmount(
                (details.getServiceFee() != null ? details.getServiceFee() : 0.0)
                        + (details.getTlsFee() != null ? details.getTlsFee() : 0.0)
        );
        tarif.setCurrency(details.getCurrency());
        tarif.setDescription(details.getDescription());
        tarif.setActive(details.getActive());
        return repository.save(tarif);
    }

    // ✅ Nouveau : mettre à jour en masse (pour la grille 3x3)
    @Transactional
    public void saveAll(List<VisaTarif> tarifs) {
        for (VisaTarif tarif : tarifs) {
            tarif.setTotalAmount(
                    (tarif.getServiceFee() != null ? tarif.getServiceFee() : 0.0)
                            + (tarif.getTlsFee() != null ? tarif.getTlsFee() : 0.0)
            );
        }
        repository.saveAll(tarifs);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}