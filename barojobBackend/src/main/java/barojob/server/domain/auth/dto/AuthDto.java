package barojob.server.domain.auth.dto;

import barojob.server.domain.user.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import barojob.server.domain.user.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AuthDto {

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class SignInRequest{
        private String email;
        private String password;

        public static SignInRequest of(String email, String password){
            return SignInRequest.builder()
                    .email(email)
                    .password(password)
                    .build();
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class SignUpRequest{
        private String email;

        private String nickname;

        private String password;

        public User toEntity(PasswordEncoder encoder){
            return User.builder()
                    .email(email)
                    .nickname(nickname)
                    .password(encoder.encode(password))
                    .build();
        }

        public static SignUpRequest of(String email, String nickname, String password){
            return SignUpRequest.builder()
                    .email(email)
                    .nickname(nickname)
                    .password(password)
                    .build();
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class SessionIdResponse{
        private UserDto.UserResponse user;
        private String sessionId;

        public static SessionIdResponse of(User user, String sessionId){
            return SessionIdResponse.builder()
                    .user(UserDto.UserResponse.from(user))
                    .sessionId(sessionId)
                    .build();
        }

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class SignUpResponse{
        private UserDto.UserResponse user;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class ValidSessionTestResponse{
        private String nickname;
    }

}
