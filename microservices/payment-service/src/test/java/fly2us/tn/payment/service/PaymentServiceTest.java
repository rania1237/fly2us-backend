package fly2us.tn.payment.service;

import fly2us.tn.payment.client.DossierClient;
import fly2us.tn.payment.dto.DossierDTO;
import fly2us.tn.payment.model.Payment;
import fly2us.tn.payment.model.VisaTarif;
import fly2us.tn.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private VisaTarifService visaTarifService;

    @Mock
    private PaymentSimulator simulator;

    @Mock
    private EmailService emailService;

    @Mock
    private DossierClient dossierClient;

    @InjectMocks
    private PaymentService paymentService;

    // Objet réutilisable pour plusieurs tests
    private DossierDTO dossier;
    private VisaTarif tarif;

    @BeforeEach
    public void setUp() {
        dossier = new DossierDTO();
        dossier.setId(1L);
        dossier.setClientId(10L);
        dossier.setType("ETUDIANT");
        dossier.setPriority("NORMAL");

        tarif = new VisaTarif();
        tarif.setTotalAmount(150.0);
        tarif.setCurrency("TND");
    }

    // ==========================================================
    // TEST 1 : Récupérer le tarif d'un dossier
    // ==========================================================
    @Test
    public void testGetTarifForDossier_ShouldReturnTarif() {
        // ARRANGE
        when(dossierClient.getDossierById(1L)).thenReturn(dossier);
        when(visaTarifService.getTarifFor("ETUDIANT", "NORMAL")).thenReturn(tarif);

        // ACT
        VisaTarif result = paymentService.getTarifForDossier(1L);

        // ASSERT
        assertNotNull(result);
        assertEquals(150.0, result.getTotalAmount());
        assertEquals("TND", result.getCurrency());
        verify(dossierClient, times(1)).getDossierById(1L);
    }

    // ==========================================================
    // TEST 2 : Initier un paiement réussi (cas nominal)
    // ==========================================================
    @Test
    public void testInitiatePayment_WhenSuccess_ShouldSetStatusPaid() throws Exception {
        // ARRANGE
        when(dossierClient.getDossierById(1L)).thenReturn(dossier);
        when(paymentRepository.findByDossierId(1L)).thenReturn(Collections.emptyList());
        when(visaTarifService.getTarifFor("ETUDIANT", "NORMAL")).thenReturn(tarif);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));
        when(simulator.processPayment(any(Payment.class), anyString())).thenReturn(true);
        doNothing().when(emailService).sendPaymentConfirmation(any(), any(), any(), anyString());

        // ACT
        Payment result = paymentService.initiatePayment(
                1L, 10L, "CARTE_BANCAIRE",
                "1234567890123456", "Rania Regei", "rania@fly2us.tn"
        );

        // ASSERT
        assertNotNull(result);
        assertEquals("PAID", result.getStatus(), "Le statut doit être PAID");
        assertEquals(150.0, result.getAmount());
        assertEquals("TND", result.getCurrency());
        assertNotNull(result.getPaidAt(), "La date de paiement doit être remplie");
        assertNotNull(result.getTransactionId());
        assertTrue(result.getTransactionId().startsWith("TXN-"));
        verify(simulator, times(1)).processPayment(any(Payment.class), anyString());
        verify(emailService, times(1)).sendPaymentConfirmation(any(), any(), any(), anyString());
    }

    // ==========================================================
    // TEST 3 : Paiement refusé par la banque → statut FAILED
    // ==========================================================
    @Test
    public void testInitiatePayment_WhenBankRefuses_ShouldSetStatusFailed() throws Exception {
        // ARRANGE
        when(dossierClient.getDossierById(1L)).thenReturn(dossier);
        when(paymentRepository.findByDossierId(1L)).thenReturn(Collections.emptyList());
        when(visaTarifService.getTarifFor("ETUDIANT", "NORMAL")).thenReturn(tarif);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));
        when(simulator.processPayment(any(Payment.class), anyString())).thenReturn(false);

        // ACT
        Payment result = paymentService.initiatePayment(
                1L, 10L, "CARTE_BANCAIRE",
                "1234567890123456", "Rania Regei", "rania@fly2us.tn"
        );

        // ASSERT
        assertEquals("FAILED", result.getStatus());
        assertEquals("Paiement refusé par la banque", result.getFailureReason());
        // L'email ne doit PAS être envoyé si le paiement échoue
        verify(emailService, never()).sendPaymentConfirmation(any(), any(), any(), anyString());
    }

    // ==========================================================
    // TEST 4 : Paiement déjà effectué pour ce dossier → Exception
    // ==========================================================
    @Test
    public void testInitiatePayment_WhenAlreadyPaid_ShouldThrowException() {
        // ARRANGE
        Payment existingPaidPayment = new Payment();
        existingPaidPayment.setStatus("PAID");

        when(dossierClient.getDossierById(1L)).thenReturn(dossier);
        when(paymentRepository.findByDossierId(1L)).thenReturn(Arrays.asList(existingPaidPayment));

        // ACT + ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.initiatePayment(1L, 10L, "CARTE_BANCAIRE",
                    "1234567890123456", "Rania", "rania@fly2us.tn");
        });

        assertEquals("Ce dossier a déjà été payé", exception.getMessage());
        verify(simulator, never()).processPayment(any(), anyString());
    }

    // ==========================================================
    // TEST 5 : Extraction des 4 derniers chiffres de la carte
    // ==========================================================
    @Test
    public void testInitiatePayment_WithCard_ShouldExtractLast4Digits() throws Exception {
        // ARRANGE
        when(dossierClient.getDossierById(1L)).thenReturn(dossier);
        when(paymentRepository.findByDossierId(1L)).thenReturn(Collections.emptyList());
        when(visaTarifService.getTarifFor(anyString(), anyString())).thenReturn(tarif);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));
        when(simulator.processPayment(any(Payment.class), anyString())).thenReturn(true);
        doNothing().when(emailService).sendPaymentConfirmation(any(), any(), any(), anyString());

        // ACT
        Payment result = paymentService.initiatePayment(
                1L, 10L, "CARTE_BANCAIRE",
                "4111222233334444", "Rania Regei", "rania@fly2us.tn"
        );

        // ASSERT
        assertEquals("4444", result.getCardLast4(), "Les 4 derniers chiffres doivent être extraits");
        assertEquals("Rania Regei", result.getCardHolder());
    }

    // ==========================================================
    // TEST 6 : Paiement échoue à cause d'une exception du simulateur
    // ==========================================================
    @Test
    public void testInitiatePayment_WhenSimulatorThrows_ShouldSetStatusFailed() throws Exception {
        // ARRANGE
        when(dossierClient.getDossierById(1L)).thenReturn(dossier);
        when(paymentRepository.findByDossierId(1L)).thenReturn(Collections.emptyList());
        when(visaTarifService.getTarifFor(anyString(), anyString())).thenReturn(tarif);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));
        when(simulator.processPayment(any(Payment.class), anyString()))
                .thenThrow(new RuntimeException("Erreur réseau bancaire"));

        // ACT
        Payment result = paymentService.initiatePayment(
                1L, 10L, "CARTE_BANCAIRE",
                "1234567890123456", "Rania", "rania@fly2us.tn"
        );

        // ASSERT
        assertEquals("FAILED", result.getStatus());
        assertEquals("Erreur réseau bancaire", result.getFailureReason());
    }

    // ==========================================================
    // TEST 7 : Récupérer tous les paiements
    // ==========================================================
    @Test
    public void testGetAll_ShouldReturnAllPayments() {
        // ARRANGE
        Payment p1 = new Payment();
        p1.setStatus("PAID");
        Payment p2 = new Payment();
        p2.setStatus("PENDING");

        when(paymentRepository.findAll()).thenReturn(Arrays.asList(p1, p2));

        // ACT
        List<Payment> result = paymentService.getAll();

        // ASSERT
        assertEquals(2, result.size());
        verify(paymentRepository, times(1)).findAll();
    }

    // ==========================================================
    // TEST 8 : Récupérer les paiements d'un client
    // ==========================================================
    @Test
    public void testGetByClient_ShouldReturnClientPayments() {
        // ARRANGE
        Payment p1 = new Payment();
        p1.setClientId(10L);
        Payment p2 = new Payment();
        p2.setClientId(10L);

        when(paymentRepository.findByClientId(10L)).thenReturn(Arrays.asList(p1, p2));

        // ACT
        List<Payment> result = paymentService.getByClient(10L);

        // ASSERT
        assertEquals(2, result.size());
        verify(paymentRepository, times(1)).findByClientId(10L);
    }

    // ==========================================================
    // TEST 9 : Vérifier si un dossier est payé (cas OUI)
    // ==========================================================
    @Test
    public void testIsDossierPaid_WhenPaid_ShouldReturnTrue() {
        // ARRANGE
        Payment paid = new Payment();
        paid.setStatus("PAID");

        when(paymentRepository.findByDossierId(1L)).thenReturn(Arrays.asList(paid));

        // ACT
        boolean result = paymentService.isDossierPaid(1L);

        // ASSERT
        assertTrue(result, "Le dossier doit être considéré comme payé");
    }

    // ==========================================================
    // TEST 10 : Rembourser un paiement
    // ==========================================================
    @Test
    public void testRefund_ShouldChangeStatusToRefunded() {
        // ARRANGE
        Long paymentId = 5L;
        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setStatus("PAID");

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        // ACT
        Payment result = paymentService.refund(paymentId);

        // ASSERT
        assertEquals("REFUNDED", result.getStatus(), "Le statut doit passer à REFUNDED");
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }
}