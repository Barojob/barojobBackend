package barojob.server.domain.worker.dto;

import barojob.server.domain.worker.entity.Worker;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

public final class WorkerDto {
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateRequest {
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String email;

        @NotBlank(message = "닉네임은 필수입니다.")
        private String nickname;

        @NotBlank(message = "비밀번호는 필수입니다.")
        private String password;

        @NotBlank(message = "근로자 이름은 필수입니다.")
        private String name;

        @NotBlank(message = "전화번호는 필수입니다.")
        private String phoneNumber;

        private Double priorityScore;

        public Worker toEntity(PasswordEncoder passwordEncoder) {
            return Worker.builder()
                    .email(this.email)
                    .nickname(this.nickname)
                    .password(passwordEncoder.encode(this.password))
                    .name(this.name)
                    .phoneNumber(this.phoneNumber)
                    .priorityScore(this.priorityScore != null ? this.priorityScore : 50.0)
                    .build();
        }
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateResponse {
        private Long userId;
    }
}
