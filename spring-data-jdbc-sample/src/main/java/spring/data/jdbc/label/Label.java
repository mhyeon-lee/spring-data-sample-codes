package spring.data.jdbc.label;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.event.AfterSaveEvent;
import spring.data.jdbc.repo.Repo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.UUID;

@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class Label implements Persistable<UUID> {
    @Id
    private UUID id;

    private AggregateReference<Repo, @NotBlank @Size(max = 200) String> repoId;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Size(max = 20)
    private String color;

    @Transient
    private boolean isNew = true;

    public Label(AggregateReference<Repo, String> repoId, String name, String color) {
        this.id = UUID.randomUUID();
        this.repoId = repoId;
        this.name = name;
        this.color = color;
    }

    @PersistenceConstructor
    private Label(UUID id, AggregateReference<Repo, String> repoId, String name, String color) {
        this.id = id;
        this.repoId = repoId;
        this.name = name;
        this.color = color;
        this.isNew = false;
    }

    public static class LabelAfterSaveEventListener implements ApplicationListener<AfterSaveEvent<?>>, Ordered {
        @Override
        public void onApplicationEvent(AfterSaveEvent<?> event) {
            if (event.getEntity() instanceof Label) {
                ((Label) event.getEntity()).isNew = false;
            }
        }

        @Override
        public int getOrder() {
            return Ordered.HIGHEST_PRECEDENCE;
        }
    }
}
