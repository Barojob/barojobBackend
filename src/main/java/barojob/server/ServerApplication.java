package barojob.server;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class ServerApplication {

    public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);
	}

}
