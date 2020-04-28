package spring.data.jdbc.comment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.test.context.TestExecutionListeners;
import spring.data.jdbc.account.Account;
import spring.data.jdbc.issue.Issue;
import spring.data.jdbc.test.DataInitializeExecutionListener;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestExecutionListeners(
    listeners = DataInitializeExecutionListener.class,
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class CommentRepositoryTest {
    @Autowired
    private CommentRepository sut;

    private final AggregateReference<Issue, UUID> issue1Id = AggregateReference.to(UUID.randomUUID());
    private final AggregateReference<Account, UUID> creatorId = AggregateReference.to(UUID.randomUUID());
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
        assertThat(actual.getVersion()).isEqualTo(1L);
        assertThat(comment.getContent()).isSameAs(actual.getContent());

        Optional<Comment> load = this.sut.findById(comment.getId());
        assertThat(load).isPresent();
        assertThat(load.get().getCreatedBy()).isEqualTo(this.creatorId);
        assertThat(load.get().getContent()).isNotNull();
        assertThat(load.get().getContent().getBody()).isEqualTo("comment 1");
        assertThat(load.get().getContent().getMimeType()).isEqualTo("text/plain");
    }
}
