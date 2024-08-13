package greencity.message;

import java.io.Serializable;

import jakarta.validation.constraints.Email;
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
    private String authorEmail;
}
