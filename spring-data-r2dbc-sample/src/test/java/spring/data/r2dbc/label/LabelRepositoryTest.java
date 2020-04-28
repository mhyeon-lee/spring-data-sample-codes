package spring.data.r2dbc.label;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import spring.data.r2dbc.test.DataInitializeExecutionListener;

import javax.validation.ConstraintViolationException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestExecutionListeners(
    listeners = DataInitializeExecutionListener.class,
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class LabelRepositoryTest {
    @Autowired
    private LabelRepository sut;

    private final String repoId = "20200501120611-naver";
    private final List<Label> labels = List.of(
        new Label(this.repoId,"bug", "red"),
        new Label(this.repoId,"qa", "blue")
    );

    @Test
    void insert() {
        Label label = this.labels.get(0);

        StepVerifier.create(this.sut.save(label))
            .assertNext(actual -> {
                assertThat(label).isSameAs(actual);
                assertThat(actual.isNew()).isFalse();
            })
            .expectNextCount(0)
            .verifyComplete();

        StepVerifier.create(this.sut.findById(label.getId()))
            .assertNext(actual -> assertThat(actual.isNew()).isFalse())
            .expectNextCount(0)
            .verifyComplete();
    }

    @Test
    void insertAll() {
        StepVerifier.create(this.sut.saveAll(this.labels))
            .assertNext(actual -> {
                assertThat(this.labels.get(0)).isSameAs(actual);
                assertThat(actual.isNew()).isFalse();
            })
            .assertNext(actual -> {
                assertThat(this.labels.get(1)).isSameAs(actual);
                assertThat(actual.isNew()).isFalse();
            })
            .expectNextCount(0)
            .verifyComplete();

        StepVerifier.create(this.sut.findById(this.labels.get(0).getId()))
            .assertNext(actual -> assertThat(actual.isNew()).isFalse())
            .expectNextCount(0)
            .verifyComplete();

        StepVerifier.create(this.sut.findById(this.labels.get(1).getId()))
            .assertNext(actual -> assertThat(actual.isNew()).isFalse())
            .expectNextCount(0)
            .verifyComplete();
    }

    @Test
    void invalidNameBlank() {
        Label label = new Label(this.repoId, " ", "red");
        StepVerifier.create(this.sut.save(label))
            .expectError(ConstraintViolationException.class)
            .verify();
    }

    @Test
    void invalidSaveAll() {
        List<Label> labels = List.of(
            new Label(this.repoId, " ", "red"),
            new Label(this.repoId, "bug", " ")
        );

        StepVerifier.create(this.sut.saveAll(labels))
            .expectError(ConstraintViolationException.class)
            .verify();
    }

    @Test
    void invalidSaveAllPublisher() {
        Flux<Label> labels = Flux.just(
            new Label(this.repoId, " ", "red"),
            new Label(this.repoId, "bug", " ")
        );

        StepVerifier.create(this.sut.saveAll(labels))
            .expectError(ConstraintViolationException.class)
            .verify();
    }
}
