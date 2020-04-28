package spring.data.jdbc.comment;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import spring.data.jdbc.account.Account;
import spring.data.jdbc.issue.Issue;

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

    private AggregateReference<Issue, @NotNull UUID> issueId;

    @Valid
    @Column("ID") // PK MAPPING, default: "COMMENT"
    private CommentContent content;

    private AggregateReference<Account, @NotNull UUID> createdBy;

    @NotNull
    @PastOrPresent
    private Instant createdAt;

    public Comment(
        AggregateReference<Issue, UUID> issueId,
        CommentContent content,
        AggregateReference<Account, UUID> createdBy) {

        this.issueId = issueId;
        this.content = content;
        this.createdBy = createdBy;
        this.createdAt = Instant.now();
    }
}
