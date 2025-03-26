package barojob.server.domain.user.dto;

import barojob.server.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class UserDto {

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Data
    public static class UserResponse{
        private String email;
        private String nickname;

        public static UserResponse from(User user){
            return UserResponse.builder()
                    .email(user.getEmail())
                    .nickname(user.getNickname())
                    .build();
        }
    }
}
