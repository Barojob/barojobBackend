package barojob.server.domain.test;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CICDTestService {
    StringRedisTemplate redisTemplate;

    public ResponseEntity<?> testRedis() {
        redisTemplate.opsForValue().set("test", "test");
        return ResponseEntity.ok().build();
    }
}
