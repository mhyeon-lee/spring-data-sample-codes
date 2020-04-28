package spring.data.jpa.label;

import lombok.*;
import org.springframework.data.domain.Persistable;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.UUID;

@Entity
@Table(indexes = {@Index(columnList = "repoId")})
@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(of = "id")
@ToString
public class Label implements Persistable<UUID> {
    @Id
    private UUID id;

    @NotBlank
    @Size(max = 200)
    @Column(length = 200, nullable = false, updatable = false)
    private String repoId;

    @NotBlank
    @Size(max = 100)
    @Column(length = 100, nullable = false)
    private String name;

    @NotBlank
    @Size(max = 20)
    @Column(length = 20, nullable = false)
    private String color;

    @Transient
    private boolean isNew = true;

    public Label(String repoId, String name, String color) {
        this.id = UUID.randomUUID();
        this.repoId = repoId;
        this.name = name;
        this.color = color;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }
}
