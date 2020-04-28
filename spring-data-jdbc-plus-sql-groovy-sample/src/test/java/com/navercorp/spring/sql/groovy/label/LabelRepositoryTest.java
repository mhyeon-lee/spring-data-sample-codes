package com.navercorp.spring.sql.groovy.label;

import com.navercorp.spring.sql.groovy.repo.Repo;
import com.navercorp.spring.sql.groovy.test.DataInitializeExecutionListener;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.test.context.TestExecutionListeners;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestExecutionListeners(
    listeners = DataInitializeExecutionListener.class,
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class LabelRepositoryTest {
    @Autowired
    private LabelRepository sut;

    private final AggregateReference<Repo, String> repoId = AggregateReference.to("20200501120611-naver");
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
        assertThat(label.isNew()).isFalse();

        Optional<Label> load = this.sut.findById(label.getId());
        assertThat(load.get().isNew()).isFalse();
        assertThat(load.get().getRepoId()).isEqualTo(this.repoId);
    }
}
