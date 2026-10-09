package microservice.FLY2US;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients  // ⚠️ AJOUTER CETTE LIGNE
public class Fly2UsApplication {

	public static void main(String[] args) {
		SpringApplication.run(Fly2UsApplication.class, args);
	}
}