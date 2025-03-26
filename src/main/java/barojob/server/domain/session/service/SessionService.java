package barojob.server.domain.session.service;

import barojob.server.domain.auth.service.RedisAuthService;
import barojob.server.domain.session.repository.CustomRedisSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final RedisAuthService redisAuthService;
    private final CustomRedisSessionRepository customRedisSessionRepository;

    public String createSessionId() {
        String sessionId;
        do{
            sessionId = UUID.randomUUID().toString();
        }while(redisAuthService.isValidSessionId(sessionId));

        return sessionId;
    }

    public String getNicknameBySessionId(String sessionId) {
        return customRedisSessionRepository.getNicknameBySessionId(sessionId);
    }

}
