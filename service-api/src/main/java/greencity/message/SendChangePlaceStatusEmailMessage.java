package greencity.message;

import java.io.Serializable;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public final class SendChangePlaceStatusEmailMessage implements Serializable {
    private String authorFirstName;
    private String placeName;
    private String placeStatus;
    @Email
    @NotBlank(message = "Email cannot be blank")
    private String authorEmail;
}
