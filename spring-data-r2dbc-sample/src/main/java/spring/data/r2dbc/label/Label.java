package spring.data.r2dbc.label;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.UUID;

@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class Label implements Persistable<UUID> {
    @Id
    private UUID id;

    @NotBlank
    @Size(max = 200)
    private String repoId;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Size(max = 20)
    private String color;

    @Transient
    private boolean isNew;

    public Label(String repoId, String name, String color) {
        this.id = UUID.randomUUID();
        this.repoId = repoId;
        this.name = name;
        this.color = color;
        this.isNew = true;
    }

    @PersistenceConstructor
    private Label(UUID id, String repoId, String name, String color) {
        this.id = id;
        this.repoId = repoId;
        this.name = name;
        this.color = color;
        this.isNew = false;
    }

    public void markNotNew() {
        this.isNew = false;
    }
}
