package fly2us.tn.dossier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class VisaServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(VisaServiceApplication.class, args);
    }
}