package fly2us.tn.payment.service;

import fly2us.tn.payment.model.Payment;
import org.springframework.stereotype.Service;

@Service
public class PaymentSimulator {

    public boolean processPayment(Payment payment, String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return true;
        }

        String cleanCard = cardNumber.replaceAll("\\s", "");

        if (cleanCard.startsWith("4444")) {
            return false;  // Carte refusée
        }
        if (cleanCard.startsWith("4000")) {
            return true;   // Carte acceptée
        }
        return true;
    }
}