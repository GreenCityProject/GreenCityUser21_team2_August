package greencity.dto.events;

import greencity.dto.user.EventAuthorDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class EventForSendEmailDto {

    @NotBlank(message = "Unsubscribe token cannot be blank")
    private String unsubscribeToken;

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 70, message = "Title cannot exceed 100 characters")
    private String title;

    private List<String> imagePaths;
    private List<DatesLocationsDto> datesLocations;

    @Valid
    @NotNull(message = "Author cannot be null")
    private EventAuthorDto author;

    @NotBlank(message = "Description cannot be blank")
    private String description;
}
