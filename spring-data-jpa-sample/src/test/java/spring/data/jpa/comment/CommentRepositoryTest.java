package spring.data.jpa.comment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import spring.data.jpa.test.DataInitializeExecutionListener;

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
        // given
        Comment comment = this.comments.get(0);

        // when
        Comment actual = this.sut.save(comment);

        // then
        assertThat(actual.getVersion()).isEqualTo(0L);
        assertThat(comment.getContent()).isSameAs(actual.getContent());
    }
}
