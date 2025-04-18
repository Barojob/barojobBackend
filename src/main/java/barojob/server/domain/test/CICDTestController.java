package barojob.server.domain.test;

import barojob.server.domain.worker.repository.WorkerRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CICDTestController {

    private final CICDTestService cicdTestService;
    public CICDTestController(CICDTestService cicdTestService) {
        this.cicdTestService = cicdTestService;
    }

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

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok("4월 18일 테스트는 잘 되고 있다네");
    }



}
