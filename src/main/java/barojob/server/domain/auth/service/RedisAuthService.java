package barojob.server.domain.auth.service;

import barojob.server.domain.session.entity.UserSession;
import barojob.server.domain.session.repository.CustomRedisSessionRepository;
import barojob.server.domain.session.repository.RedisSessionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class RedisAuthService {

    private final CustomRedisSessionRepository customRedisSessionRepository;
    private final RedisSessionRepository redisSessionRepository;

    public boolean isValidSessionId(String sessionId) {
        return customRedisSessionRepository.isValidSessionId(sessionId);
    }


    public void saveSessionId(String sessionId, String email) {
        customRedisSessionRepository.saveSessionId(sessionId, email);
    }

    public void saveSession(UserSession userSession) {
        redisSessionRepository.save(userSession);
        customRedisSessionRepository.saveNicknameToSession(userSession.getNickname(), userSession.getSessionId());
    }

    public void deleteSessionId(String sessionId) {
        customRedisSessionRepository.deleteSessionId(sessionId);
    }


}

