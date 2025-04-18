package barojob.server.domain.test;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CICDTestController {

    private final CICDTestService cicdTestService;

    @GetMapping("/")
    public ResponseEntity<?> index() {
        return ResponseEntity.ok("잘 들어와졌다네");
    }

    @GetMapping("/healthcheck")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/redisTest")
    public ResponseEntity<?> redisTest() {
        return cicdTestService.testRedis();
    }



}
