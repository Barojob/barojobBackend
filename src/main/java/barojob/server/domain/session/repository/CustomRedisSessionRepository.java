package barojob.server.domain.session.repository;

import barojob.server.domain.session.entity.UserSession;
import barojob.server.domain.session.state.State;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
public class CustomRedisSessionRepository {
    private final StringRedisTemplate stringRedisTemplate;

    private final String SESSION_PREFIX = "session:";
    private final String NICKNAME_TO_SESSIONID_PREFIX = "nicknameToSessionId:";

    public boolean isValidSessionId(String sessionId) {
        try {
            Boolean exists = stringRedisTemplate.hasKey(SESSION_PREFIX + sessionId);
            if(!exists) {
            }

            return exists;
        } catch (Exception e) {
            return false;
        }
    }

    public void saveSessionId(String sessionId, String email) {
        stringRedisTemplate.opsForValue().set(sessionId, email);
    }

    public void deleteSessionId(String sessionId) {
        if(sessionId == null) return;
        Boolean result = stringRedisTemplate.delete(sessionId);
    }

    public String getNicknameBySessionId(String authorization) {
        return (String) stringRedisTemplate.opsForHash().get(SESSION_PREFIX + authorization, "nickname");
    }

    public void saveNicknameToSession(String nickname, String sessionId) {
        stringRedisTemplate.opsForValue().set(NICKNAME_TO_SESSIONID_PREFIX + nickname, sessionId,Duration.ofDays(30));
    }

    public String getNicknameToSessionId(String nickname) {
        return stringRedisTemplate.opsForValue().get(NICKNAME_TO_SESSIONID_PREFIX + nickname);
    }

    public boolean isUserSessionActive(String nickname) {
        return stringRedisTemplate.hasKey(NICKNAME_TO_SESSIONID_PREFIX + nickname);
    }

    public String getSessionIdByNickname(String nickname) {
        return stringRedisTemplate.opsForValue().get(NICKNAME_TO_SESSIONID_PREFIX + nickname);
    }

    public void setTTLToSession(String nickname, String sessionId){
        stringRedisTemplate.expire("session:" + sessionId + ":idx", 30, TimeUnit.DAYS);
        stringRedisTemplate.expire("session:nickname:" + nickname, 30, TimeUnit.DAYS);
        stringRedisTemplate.expire("session", 30, TimeUnit.DAYS); // 필요에 따라 생략 가능
    }
}
