package barojob.server.system.security.service;

import barojob.server.domain.user.entity.User;
import barojob.server.domain.user.repository.UserRepository;
import barojob.server.system.exception.model.ErrorCode;
import barojob.server.system.exception.model.RestException;
import barojob.server.system.security.model.UserDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserDetailService {
    private final UserRepository userRepository;

    public UserDetail loadUserByEmail(String email) {
        User found = userRepository.findById(email)
                .orElseThrow(() -> new RestException(ErrorCode.GLOBAL_NOT_FOUND));

        return UserDetail.from(found);
    }


}
