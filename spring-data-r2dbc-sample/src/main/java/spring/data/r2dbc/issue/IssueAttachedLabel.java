package spring.data.r2dbc.issue;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class IssueAttachedLabel {
    @Id
    @With(AccessLevel.PRIVATE)
    Long id;

    @NotNull
    UUID labelId;

    @NotNull
    @PastOrPresent
    Instant attachedAt;

    @NotNull
    UUID issueId;
}
