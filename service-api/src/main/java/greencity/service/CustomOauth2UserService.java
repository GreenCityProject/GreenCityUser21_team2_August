package greencity.service;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public interface CustomOauth2UserService {
    OidcUser loadUser(OidcUserRequest oidcUserRequest);
}
