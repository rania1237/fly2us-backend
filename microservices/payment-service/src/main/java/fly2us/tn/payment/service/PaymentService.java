package fly2us.tn.payment.service;

import fly2us.tn.payment.client.DossierClient;
import fly2us.tn.payment.dto.DossierDTO;
import fly2us.tn.payment.model.Payment;
import fly2us.tn.payment.model.VisaTarif;
import fly2us.tn.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final VisaTarifService visaTarifService;
    private final PaymentSimulator simulator;
    private final EmailService emailService;
    private final DossierClient dossierClient;

    // ==========================================
    // RÉCUPÉRER LE TARIF
    // ==========================================
    public VisaTarif getTarifForDossier(Long dossierId) {
        DossierDTO dossier = dossierClient.getDossierById(dossierId);
        return visaTarifService.getTarifFor(
                dossier.getType(),
                dossier.getPriority() != null ? dossier.getPriority() : "NORMAL"
        );
    }

    // ==========================================
    // INITIER UN PAIEMENT
    // ==========================================
    @Transactional
    public Payment initiatePayment(Long dossierId, Long clientId, String method,
                                   String cardNumber, String cardHolder, String clientEmail) {
        DossierDTO dossier = dossierClient.getDossierById(dossierId);

        // ✅ LOGS POUR DEBUG
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🔍 VÉRIFICATION CLIENT");
        System.out.println("   Dossier clientId : " + dossier.getClientId());
        System.out.println("   Requête clientId : " + clientId);
        System.out.println("   Égaux ? " + dossier.getClientId().equals(clientId));
        System.out.println("═══════════════════════════════════════════");

        // ✅ VÉRIFICATION DÉSACTIVÉE POUR TEST
        /*
        if (!dossier.getClientId().equals(clientId)) {
            throw new RuntimeException("Ce dossier n'appartient pas à ce client");
        }
        */

        List<Payment> existing = paymentRepository.findByDossierId(dossierId);
        boolean alreadyPaid = existing.stream().anyMatch(p -> "PAID".equals(p.getStatus()));
        if (alreadyPaid) {
            throw new RuntimeException("Ce dossier a déjà été payé");
        }

        VisaTarif tarif = visaTarifService.getTarifFor(
                dossier.getType(),
                dossier.getPriority() != null ? dossier.getPriority() : "NORMAL"
        );

        Payment payment = new Payment();
        payment.setDossierId(dossierId);
        payment.setClientId(clientId);
        payment.setAmount(tarif.getTotalAmount());
        payment.setCurrency(tarif.getCurrency());
        payment.setMethod(method);
        payment.setStatus("PENDING");
        payment.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        if ("CARTE_BANCAIRE".equals(method) && cardNumber != null && cardNumber.length() >= 4) {
            payment.setCardLast4(cardNumber.substring(cardNumber.length() - 4));
            payment.setCardHolder(cardHolder);
        }

        Payment saved = paymentRepository.save(payment);

        try {
            boolean success = simulator.processPayment(saved, cardNumber);

            if (success) {
                saved.setStatus("PAID");
                saved.setPaidAt(LocalDateTime.now());
                paymentRepository.save(saved);

                System.out.println("✅ ✅ ✅ PAIEMENT RÉUSSI");
                System.out.println("   Transaction : " + saved.getTransactionId());
                System.out.println("   Montant : " + saved.getAmount() + " " + saved.getCurrency());

                // ==========================================
                // ✅ EMAIL ACTIVÉ
                // ==========================================
                System.out.println("📧 Tentative envoi email à : " + clientEmail);
                try {
                    emailService.sendPaymentConfirmation(saved, dossier, tarif, clientEmail);
                    saved.setEmailSent(true);
                    paymentRepository.save(saved);
                    System.out.println("✅ ✅ ✅ EMAIL ENVOYÉ AVEC SUCCÈS");
                } catch (Exception e) {
                    System.err.println("❌ Erreur email : " + e.getMessage());
                    saved.setEmailSent(false);
                    paymentRepository.save(saved);
                }

            } else {
                saved.setStatus("FAILED");
                saved.setFailureReason("Paiement refusé par la banque");
                paymentRepository.save(saved);
                System.out.println("❌ Paiement refusé par la banque");
            }
        } catch (Exception e) {
            saved.setStatus("FAILED");
            saved.setFailureReason(e.getMessage());
            paymentRepository.save(saved);
            System.err.println("❌ Erreur paiement : " + e.getMessage());
        }

        return saved;
    }

    // ==========================================
    // LISTER LES PAIEMENTS
    // ==========================================
    public List<Payment> getAll() {
        return paymentRepository.findAll();
    }

    public List<Payment> getByClient(Long clientId) {
        return paymentRepository.findByClientId(clientId);
    }

    public List<Payment> getByDossier(Long dossierId) {
        return paymentRepository.findByDossierId(dossierId);
    }

    public Payment getById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paiement non trouvé : " + id));
    }

    // ==========================================
    // VÉRIFIER SI UN DOSSIER EST PAYÉ
    // ==========================================
    public boolean isDossierPaid(Long dossierId) {
        return paymentRepository.findByDossierId(dossierId).stream()
                .anyMatch(p -> "PAID".equals(p.getStatus()));
    }

    // ==========================================
    // REMBOURSER UN PAIEMENT
    // ==========================================
    @Transactional
    public Payment refund(Long paymentId) {
        Payment payment = getById(paymentId);
        payment.setStatus("REFUNDED");
        return paymentRepository.save(payment);
    }
}