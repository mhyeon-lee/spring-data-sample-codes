package spring.data.jdbc.issue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.transaction.support.TransactionTemplate;
import spring.data.jdbc.account.Account;
import spring.data.jdbc.label.Label;
import spring.data.jdbc.repo.Repo;
import spring.data.jdbc.test.DataInitializeExecutionListener;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestExecutionListeners(
    listeners = DataInitializeExecutionListener.class,
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class IssueRepositoryTest {
    @Autowired
    private IssueRepository sut;

    private final AggregateReference<Repo, String> repoId = AggregateReference.to("20200501120611-naver");
    private final AggregateReference<Account, UUID> creatorId = AggregateReference.to(UUID.randomUUID());
    private final List<UUID> labelIds = List.of(
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID()
    );
    private final List<Issue> issues = List.of(
        Issue.builder()
            .id(UUID.randomUUID())
            .version(0L)
            .repoId(this.repoId)
            .issueNo(1L)
            .status(Status.OPEN)
            .title("issue 1")
            .content(IssueContent.builder()
                .body("content 1")
                .mimeType("text/plain")
                .build())
            .attachedLabels(new ArrayList<>(List.of(
                IssueAttachedLabel.builder()
                    .labelId(AggregateReference.to(this.labelIds.get(0)))
                    .attachedAt(Instant.now().minus(3L, ChronoUnit.DAYS))
                    .build(),
                IssueAttachedLabel.builder()
                    .labelId(AggregateReference.to(this.labelIds.get(1)))
                    .attachedAt(Instant.now().minus(2L, ChronoUnit.DAYS))
                    .build()
            )))
            .createdBy(this.creatorId)
            .build(),
        Issue.builder()
            .id(UUID.randomUUID())
            .version(0L)
            .repoId(this.repoId)
            .issueNo(2L)
            .status(Status.OPEN)
            .title("issue 2")
            .content(IssueContent.builder()
                .body("content 2")
                .mimeType("text/plain")
                .build())
            .attachedLabels(new ArrayList<>(List.of(
                IssueAttachedLabel.builder()
                    .labelId(AggregateReference.to(this.labelIds.get(1)))
                    .attachedAt(Instant.now().minus(3L, ChronoUnit.DAYS))
                    .build(),
                IssueAttachedLabel.builder()
                    .labelId(AggregateReference.to(this.labelIds.get(2)))
                    .attachedAt(Instant.now().minus(2L, ChronoUnit.DAYS))
                    .build()
            )))
            .createdBy(this.creatorId)
            .build(),
        Issue.builder()
            .id(UUID.randomUUID())
            .version(0L)
            .repoId(this.repoId)
            .issueNo(3L)
            .status(Status.CLOSED)
            .title("issue 3")
            .content(IssueContent.builder()
                .body("content 3")
                .mimeType("text/plain")
                .build())
            .attachedLabels(new ArrayList<>(List.of(
                IssueAttachedLabel.builder()
                    .labelId(AggregateReference.to(this.labelIds.get(0)))
                    .attachedAt(Instant.now().minus(3L, ChronoUnit.DAYS))
                    .build(),
                IssueAttachedLabel.builder()
                    .labelId(AggregateReference.to(this.labelIds.get(2)))
                    .attachedAt(Instant.now().minus(2L, ChronoUnit.DAYS))
                    .build()
            )))
            .createdBy(this.creatorId)
            .build()
    );

    @Test
    void insert() {
        // given
        Issue issue = this.issues.get(0);

        // when
        Issue actual = this.sut.save(issue);

        // then
        assertThat(issue.getVersion()).isEqualTo(1L);
        assertThat(issue.getContent()).isSameAs(actual.getContent());

        assertThat(issue.getAttachedLabels().get(0)).isSameAs(actual.getAttachedLabels().get(0));
        assertThat(issue.getAttachedLabels().get(1)).isSameAs(actual.getAttachedLabels().get(1));

        Optional<Issue> load = this.sut.findById(issue.getId());
        assertThat(load).isPresent();
        assertThat(load.get().getId()).isEqualTo(issue.getId());
        assertThat(load.get().getVersion()).isEqualTo(1L);
        assertThat(load.get().getRepoId()).isEqualTo(this.repoId);
        assertThat(load.get().getStatus()).isEqualTo(Status.OPEN);
        assertThat(load.get().getTitle()).isEqualTo("issue 1");
        assertThat(load.get().getContent().getBody()).isEqualTo("content 1");
        assertThat(load.get().getContent().getMimeType()).isEqualTo("text/plain");
        assertThat(load.get().getCreatedBy()).isEqualTo(this.creatorId);
    }

    @Test
    void optimisticLockingFailure(@Autowired TransactionTemplate transactionTemplate) {
        Issue issue = this.issues.get(0);
        this.sut.save(issue);

        assertThatThrownBy(() -> {
            transactionTemplate.execute(status -> {
                Issue load = this.sut.findById(issue.getId()).get();

                // 다른 Thread 에서 먼저 commit 해서 version 을 변경시킨다.
                this.asyncChangeContent(
                    load.getId(),
                    new IssueContent("spring-data-jdbc", "text/markdown"),
                    transactionTemplate)
                    .join();

                load.changeContent(new IssueContent("spring-data-jpa", "text/plain"));
                this.sut.save(load);    // Version 이 면경되어 있어 OptimisticLocking 에러가 발생한다.
                return null;
            });
        })
            .isExactlyInstanceOf(DbActionExecutionException.class)
            .hasCauseExactlyInstanceOf(OptimisticLockingFailureException.class);

        transactionTemplate.execute(status -> {
            Optional<Issue> load = this.sut.findById(issue.getId());
            assertThat(load.get().getVersion()).isEqualTo(2L);
            assertThat(load.get().getContent().getBody()).isEqualTo("spring-data-jdbc");
            assertThat(load.get().getContent().getMimeType()).isEqualTo("text/markdown");
            return null;
        });
    }

    @Test
    void findAllPage() {
        // given
        this.sut.saveAll(this.issues);

        // when
        // SORT property 는 entity property 명으로 변경되야 한다.
        // (https://github.com/spring-projects/spring-data-jdbc/pull/210)
        Page<Issue> actual = this.sut.findAll(
            PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "ISSUE_NO")));

        // then
        assertThat(actual.getTotalPages()).isEqualTo(2);
        assertThat(actual.getTotalElements()).isEqualTo(3);
        assertThat(actual.getContent().get(0).getIssueNo()).isEqualTo(3);
        assertThat(actual.getContent().get(1).getIssueNo()).isEqualTo(2);
    }

    @Test
    void findByTitleLikeAndStatus() {
        // given
        this.sut.saveAll(this.issues);

        // SORT property 는 entity property 명으로 변경되야 한다.
        // (https://github.com/spring-projects/spring-data-jdbc/pull/210)
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "ISSUE_NO"));

        // when
        List<Issue> content = this.sut.findByTitleLikeAndStatus("issue%", Status.OPEN, pageable);
        Page<Issue> actual = PageableExecutionUtils.getPage(content, pageable, () ->
            this.sut.countByTitleLikeAndStatus("issue%",Status.OPEN));

        // then
        assertThat(actual).hasSize(2);
        assertThat(actual.getTotalPages()).isEqualTo(1);
        assertThat(actual.getTotalElements()).isEqualTo(2);
        assertThat(actual.getContent().get(0).getIssueNo()).isEqualTo(2);
        assertThat(actual.getContent().get(1).getIssueNo()).isEqualTo(1);
    }

    @Test
    void findByRepoIdAndAttachedLabelsLabelId() {
        // given
        this.sut.saveAll(this.issues);

        // when
        Page<Issue> actual = this.sut.findByRepoIdAndAttachedLabelsLabelId(
            this.repoId, AggregateReference.to(this.labelIds.get(0)), PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "ISSUE_NO")));

        // then
        assertThat(actual).hasSize(2);
        assertThat(actual.getTotalPages()).isEqualTo(1);
        assertThat(actual.getTotalElements()).isEqualTo(2);
        assertThat(actual.getContent().get(0).getIssueNo()).isEqualTo(3);
        assertThat(actual.getContent().get(1).getIssueNo()).isEqualTo(1);
    }

    private CompletableFuture<Void> asyncChangeContent(
        UUID id, IssueContent content, TransactionTemplate transactionTemplate) {

        return CompletableFuture.runAsync(() ->
            transactionTemplate.execute(status -> {
                Issue issue = this.sut.findById(id).get();
                issue.changeContent(content);
                this.sut.save(issue);
                return null;
            }));
    }
}
