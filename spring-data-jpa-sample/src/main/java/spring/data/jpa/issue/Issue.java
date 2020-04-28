package spring.data.jpa.issue;

import lombok.*;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(indexes = {
    @Index(columnList = "repoId, issueNo", unique = true),
    @Index(columnList = "repoId"),
    @Index(columnList = "createdBy"),
    @Index(columnList = "title")
})
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString
public class Issue {
    @Id
    @Column(length = 36)
    private UUID id;

    @PositiveOrZero
    @Version
    private long version;

    @NotBlank
    @Size(max = 200)
    @Column(length = 200, nullable = false, updatable = false)
    private String repoId;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, updatable = false)
    private Long issueNo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Status status;

    @NotBlank
    @Size(max = 200)
    @Column(length = 200, nullable = false)
    private String title;

    @Valid
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false, orphanRemoval = true)
    private IssueContent content;

    @Valid
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "issue_id")
    @OrderBy("attachedAt")
    @org.hibernate.annotations.BatchSize(size = 20)
    @Builder.Default
    private List<IssueAttachedLabel> attachedLabels = new ArrayList<>();

    @NotNull
    @Column(length = 36, nullable = false, updatable = false)
    private UUID createdBy;

    @NotNull
    @PastOrPresent
    @Builder.Default
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public void changeContent(IssueContent content) {
        this.content = content;
    }

    public List<IssueAttachedLabel> getAttachedLabels() {
        return Collections.unmodifiableList(this.attachedLabels);
    }
}
