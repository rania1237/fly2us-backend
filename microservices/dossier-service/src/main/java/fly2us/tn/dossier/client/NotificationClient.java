package fly2us.tn.dossier.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "notification-service", url = "http://localhost:8085")
public interface NotificationClient {

    @PostMapping("/api/notifications/email")
    void sendEmail(
            @RequestParam Long userId,
            @RequestParam String subject,
            @RequestParam String message
    );

    @PostMapping("/api/notifications/alert")
    void sendAlert(
            @RequestParam Long userId,
            @RequestParam String subject,
            @RequestParam String message
    );
}