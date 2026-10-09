package fly2us.tn.payment.controller;

import fly2us.tn.payment.dto.PaymentRequestDTO;
import fly2us.tn.payment.model.Payment;
import fly2us.tn.payment.model.VisaTarif;
import fly2us.tn.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class PaymentController {

    private final PaymentService service;

    @GetMapping
    public ResponseEntity<List<Payment>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Payment>> getByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(service.getByClient(clientId));
    }

    @GetMapping("/dossier/{dossierId}")
    public ResponseEntity<List<Payment>> getByDossier(@PathVariable Long dossierId) {
        return ResponseEntity.ok(service.getByDossier(dossierId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/tarif/{dossierId}")
    public ResponseEntity<VisaTarif> getTarif(@PathVariable Long dossierId) {
        return ResponseEntity.ok(service.getTarifForDossier(dossierId));
    }

    @GetMapping("/is-paid/{dossierId}")
    public ResponseEntity<Boolean> isPaid(@PathVariable Long dossierId) {
        return ResponseEntity.ok(service.isDossierPaid(dossierId));
    }

    @PostMapping("/initiate")
    public ResponseEntity<Payment> initiate(@RequestBody PaymentRequestDTO request) {
        Payment payment = service.initiatePayment(
                request.getDossierId(),
                request.getClientId(),
                request.getMethod(),
                request.getCardNumber(),
                request.getCardHolder(),
                request.getClientEmail()
        );
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<Payment> refund(@PathVariable Long id) {
        return ResponseEntity.ok(service.refund(id));
    }
}