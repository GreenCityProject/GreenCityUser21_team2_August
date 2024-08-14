package greencity.dto.econews;

import greencity.dto.user.PlaceAuthorDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.ZonedDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
@EqualsAndHashCode
public class EcoNewsForSendEmailDto {
    @NotBlank(message = "Unsubscribe token cannot be blank")
    private String unsubscribeToken;

    @NotNull(message = "Creation date cannot be null")
    private ZonedDateTime creationDate;

    private String imagePath;
    private String source;

    @Valid
    @NotNull(message = "Author cannot be null")
    private PlaceAuthorDto author;

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;

    @NotBlank(message = "Text cannot be blank")
    private String text;
}
