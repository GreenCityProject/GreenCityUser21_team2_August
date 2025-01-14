package greencity.security.controller;

import greencity.annotations.ApiLocale;
import greencity.annotations.ValidLanguage;
import greencity.constant.HttpStatuses;
import greencity.dto.user.UserAdminRegistrationDto;
import greencity.dto.user.UserManagementDto;
import greencity.security.dto.SuccessSignInDto;
import greencity.security.dto.SuccessSignUpDto;
import greencity.security.dto.ownsecurity.*;
import greencity.security.service.OwnSecurityService;
import greencity.security.service.PasswordRecoveryService;
import greencity.security.service.VerifyEmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Optional;

import static greencity.constant.ErrorMessage.*;
import static greencity.constant.ValidationConstants.USER_CREATED;

/**
 * Controller that provides our sign-up and sign-in logic.
 *
 * @author Nazar Stasyuk
 * @version 1.0
 */
@RestController
@RequestMapping("/ownSecurity")
@Validated
@Slf4j
public class OwnSecurityController {
    private final OwnSecurityService service;
    private final VerifyEmailService verifyEmailService;
    private final PasswordRecoveryService passwordRecoveryService;

    /**
     * Constructor.
     *
     * @param service            - {@link OwnSecurityService} - service for security
     *                           logic.
     * @param verifyEmailService {@link VerifyEmailService} - service for email
     *                           verification.
     */
    @Autowired
    public OwnSecurityController(OwnSecurityService service,
        VerifyEmailService verifyEmailService,
        PasswordRecoveryService passwordRecoveryService) {
        this.service = service;
        this.verifyEmailService = verifyEmailService;
        this.passwordRecoveryService = passwordRecoveryService;
    }

    /**
     * Method for sign-up by our security logic.
     *
     * @param dto - {@link OwnSignUpDto} that have sign-up information.
     * @return {@link ResponseEntity}
     */
    @Operation(summary = "Sign-up by own security logic")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = USER_CREATED,
            content = @Content(schema = @Schema(implementation = SuccessSignUpDto.class))),
        @ApiResponse(responseCode = "400", description = USER_ALREADY_REGISTERED_WITH_THIS_EMAIL)
    })
    @PostMapping("/signUp")
    @ApiLocale
    public ResponseEntity<SuccessSignUpDto> signUp(@Valid @RequestBody OwnSignUpDto dto,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.signUp(dto, locale.getLanguage()));
    }

    /**
     * Method for signing-up employee by our security logic.
     *
     * @param dto - {@link EmployeeSignUpDto} that have sign-up information for
     *            employee.
     * @return {@link ResponseEntity}
     */
    @Operation(summary = "Sign-up employee by own security logic")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = USER_CREATED,
            content = @Content(schema = @Schema(implementation = SuccessSignUpDto.class))),
        @ApiResponse(responseCode = "400", description = USER_ALREADY_REGISTERED_WITH_THIS_EMAIL)
    })
    @PostMapping("/sign-up-employee")
    @ApiLocale
    public ResponseEntity<SuccessSignUpDto> singUpEmployee(@Valid @RequestBody EmployeeSignUpDto dto,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.signUpEmployee(dto, locale.getLanguage()));
    }

    /**
     * Method for sign-in by our security logic.
     *
     * @param dto - {@link OwnSignInDto} that have sign-in information.
     * @return {@link ResponseEntity}
     */
    @Operation(summary = "Sign-in by own security logic")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = SuccessSignInDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST)
    })
    @PostMapping("/signIn")
    public SuccessSignInDto singIn(@Valid @RequestBody OwnSignInDto dto) {
        return service.signIn(dto);
    }

    /**
     * Method for verifying users email.
     *
     * @param token - {@link String} this is token (hash) to verify user.
     * @return {@link ResponseEntity}
     */
    @Operation(summary = "Verify email by email token (hash that contains link for verification)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = NO_ANY_EMAIL_TO_VERIFY_BY_THIS_TOKEN)
    })
    @GetMapping("/verifyEmail")
    public ResponseEntity<Boolean> verify(@RequestParam @NotBlank String token,
        @RequestParam("user_id") Long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(verifyEmailService.verifyByToken(userId, token));
    }

    /**
     * Method for refresh access token.
     *
     * @param refreshToken - {@link String} this is refresh token.
     * @return {@link ResponseEntity} - with new access token.
     */
    @Operation(summary = "Updating access token by refresh token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = REFRESH_TOKEN_NOT_VALID)
    })
    @GetMapping("/updateAccessToken")
    public ResponseEntity<Object> updateAccessToken(@RequestParam @NotBlank String refreshToken) {
        return ResponseEntity.ok().body(service.updateAccessTokens(refreshToken));
    }

    /**
     * Method for restoring password and sending email for restore.
     *
     * @param email - {@link String}
     * @return - {@link ResponseEntity}
     * @author Dmytro Dovhal
     */
    @Operation(summary = "Sending email for restore password.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = USER_NOT_FOUND_BY_EMAIL)
    })
    @GetMapping("/restorePassword")
    @ApiLocale
    public ResponseEntity<Object> restore(@RequestParam @Email String email,
        @RequestParam Optional<String> ubs, @Parameter(hidden = true) @ValidLanguage Locale locale) {
        boolean isUbs = ubs.isPresent();
        log.info(Locale.getDefault().toString());
        passwordRecoveryService.sendPasswordRecoveryEmailTo(email, isUbs, locale.getLanguage());
        return ResponseEntity.ok().build();
    }

    /**
     * Method for changing password.
     *
     * @param form - {@link OwnRestoreDto}
     * @return - {@link ResponseEntity}
     * @author Dmytro Dovhal
     */
    @Operation(summary = "Updating password for restore password option.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "404", description = TOKEN_FOR_RESTORE_IS_INVALID),
        @ApiResponse(responseCode = "400", description = PASSWORD_DOES_NOT_MATCH)
    })
    @PostMapping("/updatePassword")
    public ResponseEntity<Object> resetPassword(@Valid @RequestBody OwnRestoreDto form) {
        passwordRecoveryService.updatePasswordUsingToken(form);
        return ResponseEntity.ok().build();
    }

    /**
     * Method for updating current password.
     *
     * @param updateDto - {@link UpdatePasswordDto}
     * @return - {@link ResponseEntity}
     * @author Dmytro Dovhal
     */
    @Operation(summary = "Updating current password.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED)
    })
    @PutMapping("/changePassword")
    public ResponseEntity<Object> updatePassword(@Valid @RequestBody UpdatePasswordDto updateDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        service.updateCurrentPassword(updateDto, email);
        return ResponseEntity.ok().build();
    }

    /**
     * Register new user from admin panel.
     *
     * @param userDto - dto with info for registering user.
     * @return - {@link UserAdminRegistrationDto}
     * @author Orest Mamchuk
     */
    @Operation(summary = "Register new user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED,
            content = @Content(schema = @Schema(implementation = UserAdminRegistrationDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @PostMapping("/register")
    public ResponseEntity<UserAdminRegistrationDto> managementRegisterUser(
        @Valid @RequestBody UserManagementDto userDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.managementRegisterUser(userDto));
    }

    /**
     * Method for checking if user has password.
     *
     * @return - {@link PasswordStatusDto}
     */
    @Operation(summary = "Get password status for current user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED)
    })
    @GetMapping("/password-status")
    public ResponseEntity<PasswordStatusDto> passwordStatus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return ResponseEntity.ok().body(new PasswordStatusDto(service.hasPassword(email)));
    }

    /**
     * Method for setting password for user that doesn't have one.
     *
     * @param dto {@link SetPasswordDto} password to be set.
     * @return {@link ResponseEntity}
     */
    @Operation(summary = "Set password for user that doesn't have one.")
    @ResponseStatus(value = HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED)
    })
    @PostMapping("/set-password")
    public ResponseEntity<Object> setPassword(@Valid @RequestBody SetPasswordDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        service.setPassword(dto, email);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @Operation(summary = "Reset password for user.")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
            @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED)
    })
    @PostMapping("/reset-password")
    public ResponseEntity<Object> resetPassword(@Valid @RequestBody ResetPasswordDto dto) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        service.resetPassword(dto, email);
        return ResponseEntity.status(HttpStatus.OK).build();

    }
}
