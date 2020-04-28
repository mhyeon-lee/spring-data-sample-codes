package spring.data.r2dbc.repo;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;
import java.util.UUID;

@Getter
@EqualsAndHashCode(of = "id")
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__(@PersistenceConstructor))
@ToString
public class Repo {
    private static final DateTimeFormatter ID_PREFIX_FORMAT = DateTimeFormatter
        .ofPattern("yyyyMMddHHmmss")
        .withZone(ZoneId.of("Asia/Seoul"));

    private static String generateId(String name, Instant createdAt) {
        return new StringJoiner("-")
            .add(ID_PREFIX_FORMAT.format(createdAt))
            .add(name)
            .toString();
    }

    @Id
    private String id;

    @NotBlank
    @Size(max =  100)
    private String name;

    @Size(max = 255)
    private String description;

    @NotNull
    private UUID createdBy;

    @NotNull
    @PastOrPresent
    private Instant createdAt;

    public Repo(String name, String description, UUID createdBy) {
        this.name = name;
        this.description = description;
        this.createdAt = Instant.now();
        this.createdBy = createdBy;
        this.id = generateId(this.name, this.createdAt);
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changeDescription(String description) {
        this.description = description;
    }
}
