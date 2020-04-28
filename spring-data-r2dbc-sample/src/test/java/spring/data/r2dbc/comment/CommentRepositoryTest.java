package spring.data.r2dbc.comment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import reactor.test.StepVerifier;
import spring.data.r2dbc.test.DataInitializeExecutionListener;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestExecutionListeners(
    listeners = DataInitializeExecutionListener.class,
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class CommentRepositoryTest {
    @Autowired
    private CommentRepository sut;

    private final UUID issue1Id = UUID.randomUUID();
    private final UUID creatorId = UUID.randomUUID();
    private final List<Comment> comments = List.of(
        new Comment(this.issue1Id, new CommentContent("comment 1", "text/plain"), this.creatorId),
        new Comment(this.issue1Id, new CommentContent("comment 2", "text/plain"), this.creatorId)
    );

    @Test
    void insert() {
        Comment comment = this.comments.get(0);

        StepVerifier.create(this.sut.save(comment))
            .assertNext(actual -> {
                assertThat(actual.getId()).isNotNull();
                assertThat(actual.getVersion()).isEqualTo(1L);
            })
            .expectNextCount(0)
            .verifyComplete();

        StepVerifier.create(this.sut.findById(comment.getId()))
            .assertNext(actual -> {
                assertThat(actual.getId()).isEqualTo(comment.getId());
                assertThat(actual.getVersion()).isEqualTo(1L);
                assertThat(actual.getContent().getBody()).isEqualTo("comment 1");
                assertThat(actual.getContent().getMimeType()).isEqualTo("text/plain");
            })
            .expectNextCount(0)
            .verifyComplete();
    }
}
