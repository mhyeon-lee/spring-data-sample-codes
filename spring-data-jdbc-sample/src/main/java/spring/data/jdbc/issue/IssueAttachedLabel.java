package spring.data.jdbc.issue;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import spring.data.jdbc.label.Label;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class IssueAttachedLabel {
    AggregateReference<Label, @NotNull UUID> labelId;

    @NotNull
    @PastOrPresent
    Instant attachedAt;
}
