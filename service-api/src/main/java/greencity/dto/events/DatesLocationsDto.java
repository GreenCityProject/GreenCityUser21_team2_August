package greencity.dto.events;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class DatesLocationsDto {
    @NotNull(message = "Start date is mandatory")
    private ZonedDateTime startDate;

    @NotNull(message = "Finish date is mandatory")
    private ZonedDateTime finishDate;

    private CoordinatesDto coordinates;

    @Pattern(regexp = "^(https?://).*", message = "Please add a link to the event. The link must start with http(s)://")
    private String onlineLink;
}
