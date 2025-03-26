package barojob.server.domain.session.entity;

import barojob.server.domain.session.state.State;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;

@RedisHash("session")
@AllArgsConstructor
@Getter
@Builder
public class UserSession implements Serializable {
    @Id
    private String sessionId;

    @Indexed
    private String nickname;
    private State state;
    private String roomId;
    private boolean isInGame;
    private boolean isInRoom;

    public static UserSession of(String nickname, State state, String sessionId, String roomId, boolean isInGame, boolean isInRoom){
        return UserSession.builder()
                .nickname(nickname)
                .state(state)
                .sessionId(sessionId)
                .roomId(roomId)
                .isInGame(isInGame)
                .isInRoom(isInRoom)
                .build();
    }

}