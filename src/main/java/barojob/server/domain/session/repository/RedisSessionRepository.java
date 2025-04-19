package barojob.server.domain.session.repository;

import barojob.server.domain.session.entity.UserSession;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;


public interface RedisSessionRepository extends CrudRepository<UserSession, String> {

    Optional<UserSession> findByNickname(String nickname);
}

