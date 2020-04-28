package spring.data.jpa.label;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import spring.data.jpa.test.DataInitializeExecutionListener;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestExecutionListeners(
    listeners = DataInitializeExecutionListener.class,
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class LabelRepositoryTest {
    @Autowired
    private LabelRepository sut;

    private final String repoId = "20200501120611-naver";
    private final List<Label> labels = List.of(
        new Label(this.repoId, "bug", "red")
    );

    @Test
    void insert() {
        // given
        Label label = this.labels.get(0);

        // when
        Label actual = this.sut.save(label);

        // then
        assertThat(label).isSameAs(actual);
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.isNew()).isFalse();

        Optional<Label> load = this.sut.findById(label.getId());
        assertThat(load.get().isNew()).isFalse();
    }

    @Test
    void invalidNameBlank() {
        Label label = new Label(this.repoId, " ", "red");
        assertThatThrownBy(() -> this.sut.save(label))
            .extracting(ex -> ex.getCause().getCause())
            .isExactlyInstanceOf(ConstraintViolationException.class);
    }
}
