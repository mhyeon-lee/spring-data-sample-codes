package spring.data.r2dbc.issue;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.Instant;
import java.util.UUID;

@Builder
@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class Issue {
    @Id
    private UUID id;

    @PositiveOrZero
    @Version
    private long version;

    @NotBlank
    @Size(max = 200)
    private String repoId;

    @NotNull
    @PositiveOrZero
    private Long issueNo;

    @NotNull
    private Status status;

    @NotBlank
    @Size(max = 200)
    private String title;

    @Valid
    private IssueContent content;

    @NotNull
    private UUID createdBy;

    @NotNull
    @PastOrPresent
    @Builder.Default
    private Instant createdAt = Instant.now();

    public void changeContent(IssueContent content) {
        this.content = content;
    }
}
