package spring.data.jdbc.issue;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import spring.data.jdbc.account.Account;
import spring.data.jdbc.repo.Repo;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    private AggregateReference<Repo, @NotBlank @Size(max = 200) String> repoId;

    @NotNull
    @PositiveOrZero
    private Long issueNo;

    @NotNull
    private Status status;

    @NotBlank
    @Size(max = 200)
    private String title;

    @Valid
    @Column("ISSUE_ID") // default: "ISSUE"
    private IssueContent content;

    @Valid
    @MappedCollection(idColumn = "ISSUE_ID", keyColumn = "ATTACHED_AT")
    @Builder.Default
    private List<IssueAttachedLabel> attachedLabels = new ArrayList<>();

    private AggregateReference<Account, @NotNull UUID> createdBy;

    @NotNull
    @PastOrPresent
    @Builder.Default
    private Instant createdAt = Instant.now();

    public void changeContent(IssueContent content) {
        this.content = content;
    }

    public List<IssueAttachedLabel> getAttachedLabels() {
        return Collections.unmodifiableList(this.attachedLabels);
    }
}
