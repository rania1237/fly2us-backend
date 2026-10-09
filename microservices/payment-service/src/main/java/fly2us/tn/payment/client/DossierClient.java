package fly2us.tn.payment.client;

import fly2us.tn.payment.dto.DossierDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "dossier-service", url = "http://localhost:8082")
public interface DossierClient {

    @GetMapping("/api/dossiers/{id}")
    DossierDTO getDossierById(@PathVariable("id") Long id);
}