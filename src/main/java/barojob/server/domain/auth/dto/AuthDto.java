package barojob.server.domain.auth.dto;

import barojob.server.domain.user.dto.UserDto;
import barojob.server.domain.worker.entity.Worker;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import barojob.server.domain.user.entity.User;
import org.hibernate.annotations.Check;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AuthDto {

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class SignInRequest{
        private String verificationCode;
        private String phoneNumber;
        private String role;
        public static SignInRequest of(String verificationCode,String phoneNumber,String role){
            return SignInRequest.builder()
                    .verificationCode(verificationCode)
                    .phoneNumber(phoneNumber)
                    .role(role)
                    .build();
        }
    }
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class SignInVerificateRequest{
        private String role;
        private String phoneNumber;
        public static SignInVerificateRequest of(String phoneNumber,String role){
            return SignInVerificateRequest.builder()
                    .phoneNumber(phoneNumber)
                    .role(role)
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

        //worker, employer signUp
        private String phoneNumber;

        private String name;

        private String businessName;
        public User toEntity(PasswordEncoder encoder){
            return User.builder()
                    .email(email)
                    .nickname(nickname)
                    .password(encoder.encode(password))
                    .build();
        }

        //Worker, Employer SignUpRequest
        public static SignUpRequest of(String email, String nickname, String password, String phoneNumber,String name,String businessName){
            return SignUpRequest.builder()
                    .email(email)
                    .nickname(nickname)
                    .password(password)
                    .phoneNumber(phoneNumber)
                    .name(name)
                    .businessName(businessName)
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
