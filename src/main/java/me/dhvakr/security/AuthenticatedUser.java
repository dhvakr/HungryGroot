package me.dhvakr.security;

import me.dhvakr.jpa.entity.Groots;
import me.dhvakr.jpa.service.GrootRepository;
import com.vaadin.flow.spring.security.AuthenticationContext;
import java.util.Optional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUser {

    //~ Static fields/initializers =========================================================================================================

    private final GrootRepository grootRepository;
    private final AuthenticationContext authenticationContext;

    //~ Constructor ========================================================================================================================

    public AuthenticatedUser(AuthenticationContext authenticationContext, GrootRepository grootRepository) {
        this.grootRepository = grootRepository;
        this.authenticationContext = authenticationContext;
    }

    //~ Methods ============================================================================================================================

    public Optional<Groots> get() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class)
                .map(userDetails -> grootRepository.findByUsername(userDetails.getUsername()));
    }

    //~ ====================================================================================================================================

    public Optional<OidcUser> getOidcUser() {
        return authenticationContext.getAuthenticatedUser(OidcUser.class);
    }

    //~ ====================================================================================================================================

    public void logout() {
        authenticationContext.logout();
    }
}
