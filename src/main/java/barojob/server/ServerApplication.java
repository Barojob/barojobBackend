package barojob.server;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@RequiredArgsConstructor
@EnableJpaAuditing
@EnableScheduling
public class ServerApplication {

    public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);
	}

}
