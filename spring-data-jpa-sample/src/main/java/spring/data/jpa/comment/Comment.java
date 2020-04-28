package spring.data.jpa.comment;

import lombok.*;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.PositiveOrZero;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(indexes = {
    @Index(columnList = "issueId"),
    @Index(columnList = "createdBy")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(of = "id")
@ToString
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @PositiveOrZero
    @Version
    private long version;

    @NotNull
    private UUID issueId;

    @Valid
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = false, orphanRemoval = true)
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
