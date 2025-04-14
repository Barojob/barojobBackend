package barojob.server.system.auditor;

import barojob.server.domain.user.entity.User;
import barojob.server.system.security.model.UserDetail;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<User> {

    @Override
    public Optional<User> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetail) {
            UserDetail userDetail = (UserDetail) principal;
            return Optional.ofNullable(userDetail.getUser());
        } else if (principal instanceof User) {
            return Optional.of((User) principal);
        }

        return Optional.empty();
    }
}
