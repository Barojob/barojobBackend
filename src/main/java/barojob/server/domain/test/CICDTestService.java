package barojob.server.domain.test;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CICDTestService {
    StringRedisTemplate redisTemplate;

    public ResponseEntity<?> testRedis() {
        redisTemplate.opsForValue().set("test" + UUID.randomUUID().toString(), "test");
        return ResponseEntity.ok().build();
    }
}
