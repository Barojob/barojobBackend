package barojob.server.domain.employer.dto;

import barojob.server.domain.employer.entity.Employer;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

public final class EmployerDto {
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateRequest {

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String email;

        @NotBlank(message = "닉네임은 필수입니다.")
        private String nickname;

        @NotBlank(message = "비밀번호는 필수입니다.")
        private String password;

        @NotBlank(message = "사업자명은 필수입니다.")
        @Size(max = 255, message = "사업자명은 255자 이하로 입력해주세요.")
        private String businessName;

        @NotBlank(message = "담당자 이름은 필수입니다.")
        private String name;

        @NotBlank(message = "전화번호는 필수입니다.")
        private String phoneNumber;

        public Employer toEntity(PasswordEncoder passwordEncoder) {
            return Employer.builder()
                    .email(this.email)
                    .nickname(this.nickname)
                    .password(passwordEncoder.encode(this.password))
                    .businessName(this.businessName)
                    .name(this.name)
                    .phoneNumber(this.phoneNumber)
                    .build();
        }
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateResponse {
        private Long userId;
    }
}
