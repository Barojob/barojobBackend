package barojob.server.domain.user.entity;

import barojob.server.common.timebaseentity.TimeStampedEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@SuperBuilder
@Setter
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "users")
public class User extends TimeStampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String nickname;

    private String password;
}
