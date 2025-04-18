package barojob.server.domain.test;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CICDTestService {
    private final StringRedisTemplate redisTemplate;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public ResponseEntity<?> testRedis() {
        try {
            redisTemplate.opsForValue()
                    .set("test"+UUID.randomUUID(), "test");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Redis 쓰기 실패", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("exception", e.getClass().getSimpleName(),
                            "message", e.getMessage()));
        }
    }
}
