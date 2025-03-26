package barojob.server.system.security.model;

import barojob.server.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.AuthenticatedPrincipal;

@SuperBuilder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserDetail implements AuthenticatedPrincipal {
    private User user;

    private Long id;
    private String email;
    private String nickname;

    @JsonIgnore
    @Override
    public String getName() {
        return email;
    }

    public static UserDetail from(User user) {
        return UserDetail.builder()
                .user(user)
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }
}