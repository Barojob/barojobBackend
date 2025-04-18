package barojob.server.domain.test;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class CICDTestService {
    private final StringRedisTemplate redisTemplate;

    public CICDTestService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public ResponseEntity<?> testRedis() {
        try {
            redisTemplate.opsForValue()
                    .set("test"+UUID.randomUUID(), "test");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.out.println("에러가 발생했음");
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("exception", e.getClass().getSimpleName(),
                            "message", e.getMessage(),
                            "debug", "여기서 걸린게 맞음"));
        }
    }
}
