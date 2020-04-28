package spring.data.r2dbc.comment;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.PositiveOrZero;
import java.time.Instant;
import java.util.UUID;

@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class Comment {
    @Id
    private Long id;

    @PositiveOrZero
    @Version
    private long version;

    @NotNull
    private UUID issueId;

    @Valid
    private CommentContent content;

    @NotNull
    private UUID createdBy;

    @NotNull
    @PastOrPresent
    private Instant createdAt;

    public Comment(UUID issueId, CommentContent content, UUID createdBy) {
        this.issueId = issueId;
        this.content = content;
        this.createdBy = createdBy;
        this.createdAt = Instant.now();
    }
}
