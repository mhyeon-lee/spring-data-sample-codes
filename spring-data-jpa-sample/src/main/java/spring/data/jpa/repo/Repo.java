package spring.data.jpa.repo;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;
import java.util.UUID;

@Entity
@Table(indexes = {
    @Index(columnList = "name", unique = true),
    @Index(columnList = "createdBy")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(of = "id")
@ToString
public class Repo {
    private static final DateTimeFormatter ID_PREFIX_FORMAT = DateTimeFormatter
        .ofPattern("yyyyMMddHHmmss")
        .withZone(ZoneId.of("Asia/Seoul"));

    public static String generateId(Repo repo) {
        if (repo.getId() != null) {
            throw new IllegalArgumentException("Repo is already set id. id: " + repo.getId());
        }

        return new StringJoiner("-")
            .add(ID_PREFIX_FORMAT.format(repo.getCreatedAt()))
            .add(repo.getName())
            .toString();
    }

    @Id
    private String id;

    @NotBlank
    @Size(max =  100)
    @Column(length = 100, nullable = false)
    private String name;

    @Size(max = 255)
    private String description;

    @NotNull
    @Column(nullable = false, updatable = false)
    private UUID createdBy;

    @NotNull
    @PastOrPresent
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public Repo(String name, String description, UUID createdBy) {
        this.name = name;
        this.description = description;
        this.createdAt = Instant.now();
        this.createdBy = createdBy;
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changeDescription(String description) {
        this.description = description;
    }

    @PrePersist
    void prePersist() {
        this.id = generateId(this);
    }
}
