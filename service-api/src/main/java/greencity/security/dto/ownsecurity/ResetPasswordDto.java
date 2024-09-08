package greencity.security.dto.ownsecurity;

import greencity.annotations.PasswordValidation;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class ResetPasswordDto {
    @NotBlank
    @PasswordValidation
    private String currentPassword;

    @NotBlank
    @PasswordValidation
    private String newPassword;

    @NotBlank
    @PasswordValidation
    private String confirmPassword;
}
