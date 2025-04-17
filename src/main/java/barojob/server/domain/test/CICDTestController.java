package barojob.server.domain.test;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CICDTestController {

    @GetMapping("/")
    public ResponseEntity<?> index() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok().build();
    }



}
