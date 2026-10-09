package fly2us.tn.payment.service;

import fly2us.tn.payment.dto.DossierDTO;
import fly2us.tn.payment.model.Payment;
import fly2us.tn.payment.model.VisaTarif;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // ✅ Adresse fixe (Mailtrap accepte n'importe quel from)
    private final String fromEmail = "noreply@fly2us.tn";

    public void sendPaymentConfirmation(Payment payment, DossierDTO dossier, VisaTarif tarif, String clientEmail) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("📧 DÉBUT ENVOI EMAIL");
        System.out.println("   From : " + fromEmail);
        System.out.println("   To   : " + clientEmail);
        System.out.println("   Dossier : " + dossier.getDossierNumber());
        System.out.println("═══════════════════════════════════════════");

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(clientEmail != null && !clientEmail.isEmpty() ? clientEmail : "client@example.com");
            helper.setSubject("✅ Paiement confirmé - Dossier " + dossier.getDossierNumber());
            helper.setText(buildEmailHtml(payment, dossier, tarif), true);

            System.out.println("📤 Envoi en cours...");
            mailSender.send(message);
            System.out.println("✅ ✅ ✅ EMAIL ENVOYÉ AVEC SUCCÈS à " + clientEmail);

        } catch (Exception e) {
            System.err.println("═══════════════════════════════════════════");
            System.err.println("❌ ❌ ❌ ERREUR ENVOI EMAIL");
            System.err.println("   Type : " + e.getClass().getName());
            System.err.println("   Message : " + e.getMessage());
            System.err.println("═══════════════════════════════════════════");
            e.printStackTrace();
            throw new RuntimeException("Erreur envoi email", e);
        }
    }

    private String buildEmailHtml(Payment payment, DossierDTO dossier, VisaTarif tarif) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        return "<!DOCTYPE html>" +
                "<html><body style='font-family: Arial, sans-serif; background: #f0f4ff; padding: 20px;'>" +
                "<div style='max-width: 600px; margin: 0 auto; background: white; border-radius: 12px; padding: 30px;'>" +
                "<h1 style='color: #061449; text-align: center;'>✈️ FLY2US</h1>" +
                "<h2 style='color: #059669; text-align: center;'>✅ Paiement confirmé</h2>" +
                "<p>Bonjour,</p>" +
                "<p>Nous avons bien reçu votre paiement pour le dossier <strong>" + dossier.getDossierNumber() + "</strong>.</p>" +
                "<div style='background: #f8f9ff; padding: 20px; border-radius: 8px; margin: 20px 0;'>" +
                "<h3>📋 Détails du paiement</h3>" +
                "<p><strong>N° Transaction :</strong> " + payment.getTransactionId() + "</p>" +
                "<p><strong>Montant :</strong> " + payment.getAmount() + " " + payment.getCurrency() + "</p>" +
                "<p><strong>Méthode :</strong> " + payment.getMethod() + "</p>" +
                "<p><strong>Date :</strong> " + payment.getPaidAt().format(formatter) + "</p>" +
                "</div>" +
                "<div style='background: #f8f9ff; padding: 20px; border-radius: 8px; margin: 20px 0;'>" +
                "<h3>📁 Détails du dossier</h3>" +
                "<p><strong>Type de visa :</strong> " + dossier.getType() + "</p>" +
                "<p><strong>Pays :</strong> " + dossier.getCountry() + "</p>" +
                "<p><strong>Priorité :</strong> " + dossier.getPriority() + "</p>" +
                "</div>" +
                "<p style='text-align: center; color: #888; font-size: 12px;'>© 2026 FLY2US</p>" +
                "</div></body></html>";
    }
}