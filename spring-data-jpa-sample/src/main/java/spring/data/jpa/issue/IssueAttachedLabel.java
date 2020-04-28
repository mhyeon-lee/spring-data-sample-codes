package spring.data.jpa.issue;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(indexes = {@Index(columnList = "labelId")})
@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(of = "id")
@ToString
public class IssueAttachedLabel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, updatable = false)
    private UUID labelId;

    @NotNull
    @PastOrPresent
    @Column(nullable = false, updatable = false)
    private Instant attachedAt;

    public IssueAttachedLabel(UUID labelId, Instant attachedAt) {
        this.labelId = labelId;
        this.attachedAt = attachedAt;
    }
}
