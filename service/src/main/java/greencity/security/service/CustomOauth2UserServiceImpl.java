package greencity.security.service;

import greencity.constant.AppConstant;
import greencity.entity.User;
import greencity.enums.EmailNotification;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import greencity.repository.UserRepo;
import greencity.security.jwt.JwtTool;
import greencity.service.CustomOauth2UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class CustomOauth2UserServiceImpl extends OidcUserService implements CustomOauth2UserService {

    private final JwtTool jwtTool;
    private final UserRepo userRepo;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        return processUser(oidcUser);
    }

    private OidcUser processUser(OidcUser oidcUser) {
        Map<String, Object> userData =  oidcUser.getAttributes();

        String email = (String) userData.get("email");
        if (email==null || email.isBlank()) {
            throw new OAuth2AuthenticationException("Email is not present in Oauth2User");
        }

        if (userRepo.existsUserByEmail(email)) {
            return oidcUser;
        }

        String username = (String) userData.get("name");
        if (username==null || username.isBlank()) {
            username=email.split("@")[0];
        }

        String refreshTokenKey = userData.get("refresh_token_key") != null
                ? userData.get("refresh_token_key").toString()
                : jwtTool.generateTokenKey();

        User user = User.builder()
                .name(username)
                .email(email)
                .role(Role.ROLE_USER)
                .dateOfRegistration(LocalDateTime.now())
                .role(Role.ROLE_USER)
                .refreshTokenKey(refreshTokenKey)
                .lastActivityTime(LocalDateTime.now())
                .userStatus(UserStatus.CREATED)
                .emailNotification(EmailNotification.DISABLED)
                .rating(AppConstant.DEFAULT_RATING)
                .build();

        userRepo.save(user);

        return oidcUser;
    }

}
