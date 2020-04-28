package spring.data.r2dbc.issue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import spring.data.r2dbc.test.DataInitializeExecutionListener;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestExecutionListeners(
    listeners = DataInitializeExecutionListener.class,
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class IssueRepositoryTest {
    @Autowired
    private IssueRepository sut;

    @Autowired
    private IssueAttachedLabelRepository issueAttachedLabelRepository;

    private final String repoId = "20200501120611-naver";
    private final UUID creatorId = UUID.randomUUID();
    private final List<Issue> issues = List.of(
        Issue.builder()
            .id(UUID.randomUUID())
            .repoId(this.repoId)
            .issueNo(1L)
            .version(0L)
            .status(Status.OPEN)
            .title("issue 1")
            .content(IssueContent.builder()
                .body("content 1")
                .mimeType("text/plain")
                .build())
            .createdBy(this.creatorId)
            .build(),
        Issue.builder()
            .id(UUID.randomUUID())
            .repoId(this.repoId)
            .issueNo(2L)
            .version(0L)
            .status(Status.OPEN)
            .title("issue 2")
            .content(IssueContent.builder()
                .body("content 2")
                .mimeType("text/plain")
                .build())
            .createdBy(this.creatorId)
            .build(),
        Issue.builder()
            .id(UUID.randomUUID())
            .repoId(this.repoId)
            .issueNo(3L)
            .version(0L)
            .status(Status.CLOSED)
            .title("issue 3")
            .content(IssueContent.builder()
                .body("content 3")
                .mimeType("text/plain")
                .build())
            .createdBy(this.creatorId)
            .build()
    );

    private final List<UUID> labelIds = List.of(
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID()
    );

    private final List<IssueAttachedLabel> issueAttachedLabels = List.of(
        IssueAttachedLabel.builder()
            .labelId(this.labelIds.get(0))
            .attachedAt(Instant.now().minus(3L, ChronoUnit.DAYS))
            .issueId(this.issues.get(0).getId())
            .build(),
        IssueAttachedLabel.builder()
            .labelId(this.labelIds.get(1))
            .attachedAt(Instant.now().minus(2L, ChronoUnit.DAYS))
            .issueId(this.issues.get(0).getId())
            .build(),
        IssueAttachedLabel.builder()
            .labelId(this.labelIds.get(1))
            .attachedAt(Instant.now().minus(3L, ChronoUnit.DAYS))
            .issueId(this.issues.get(1).getId())
            .build(),
        IssueAttachedLabel.builder()
            .labelId(this.labelIds.get(2))
            .attachedAt(Instant.now().minus(2L, ChronoUnit.DAYS))
            .issueId(this.issues.get(1).getId())
            .build(),
        IssueAttachedLabel.builder()
            .labelId(this.labelIds.get(0))
            .attachedAt(Instant.now().minus(3L, ChronoUnit.DAYS))
            .issueId(this.issues.get(2).getId())
            .build(),
        IssueAttachedLabel.builder()
            .labelId(this.labelIds.get(2))
            .attachedAt(Instant.now().minus(2L, ChronoUnit.DAYS))
            .issueId(this.issues.get(2).getId())
            .build()
    );

    @Test
    void insert(@Autowired TransactionalOperator operator) {
        Issue issue = this.issues.get(0);
        List<IssueAttachedLabel> labels = this.issueAttachedLabels.stream()
            .filter(attached -> attached.getIssueId().equals(issue.getId()))
            .collect(toList());

        StepVerifier.create(
            this.sut.save(issue)
                .flatMap(actual -> this.issueAttachedLabelRepository.saveAll(labels)
                    .then(Mono.just(actual)))
                .as(operator::transactional)
        )
            .assertNext(actual -> {
                assertThat(actual.getId()).isNotNull();
                assertThat(actual.getVersion()).isEqualTo(1L);
            })
            .expectNextCount(0)
            .verifyComplete();

        StepVerifier.create(this.sut.findById(issue.getId()))
            .assertNext(actual -> {
                assertThat(actual.getId()).isEqualTo(issue.getId());
                assertThat(actual.getVersion()).isEqualTo(1L);
                assertThat(actual.getStatus()).isEqualTo(Status.OPEN);
                assertThat(actual.getTitle()).isEqualTo("issue 1");
                assertThat(actual.getContent().getBody()).isEqualTo("content 1");
                assertThat(actual.getContent().getMimeType()).isEqualTo("text/plain");
            })
            .expectNextCount(0)
            .verifyComplete();

        StepVerifier.create(this.issueAttachedLabelRepository.findByIssueId(issue.getId()))
            .assertNext(actual -> assertThat(actual.getIssueId()).isEqualTo(issue.getId()))
            .assertNext(actual -> assertThat(actual.getIssueId()).isEqualTo(issue.getId()))
            .expectNextCount(0)
            .verifyComplete();
    }

    @Test
    void optimisticLockingFailure(@Autowired TransactionalOperator operator) {
        Issue issue = this.issues.get(0);
        this.sut.save(issue).block();

        StepVerifier.create(
            this.sut.findById(issue.getId())
                .doOnNext(actual ->
                    // 다른 Thread 에서 먼저 commit 해서 version 을 변경시킨다.
                    this.changeContent(
                        issue.getId(),
                        new IssueContent("spring-data-jdbc", "text/markdown"),
                        operator
                    )
                        .subscribeOn(Schedulers.boundedElastic())
                        .block()
                )
                .doOnNext(actual -> actual.changeContent(
                    new IssueContent("spring-data-jpa", "text/text")))
                .flatMap(actual -> this.sut.save(actual))
                .as(operator::transactional)
                .then())
            .verifyError(OptimisticLockingFailureException.class);

        StepVerifier.create(this.sut.findById(issue.getId()))
            .assertNext(actual -> {
                assertThat(actual.getId()).isEqualTo(issue.getId());
                assertThat(actual.getVersion()).isEqualTo(2L);
                assertThat(actual.getContent().getBody()).isEqualTo("spring-data-jdbc");
                assertThat(actual.getContent().getMimeType()).isEqualTo("text/markdown");
            })
            .expectNextCount(0)
            .verifyComplete();
    }

    @Test
    void findByTitleLikeAndStatus() {
        this.sut.saveAll(this.issues).collectList().block();

        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "issueNo"));

        StepVerifier.create(this.sut.findByTitleLikeAndStatus("issue%", Status.OPEN, pageable).collectList()
            .zipWith(this.sut.countByTitleLikeAndStatus("issue%", Status.OPEN),
                (content, count) -> new PageImpl<>(content, pageable, count)))
            .assertNext(actual -> {
                assertThat(actual).hasSize(2);
                assertThat(actual.getTotalPages()).isEqualTo(1);
                assertThat(actual.getTotalElements()).isEqualTo(2);
                assertThat(actual.getContent().get(0).getIssueNo()).isEqualTo(2);
                assertThat(actual.getContent().get(1).getIssueNo()).isEqualTo(1);
            })
            .expectNextCount(0)
            .verifyComplete();
    }

    @Test
    void findByRepoIdAndAttachedLabelsLabelId() {
        this.sut.saveAll(this.issues)
            .thenMany(this.issueAttachedLabelRepository.saveAll(this.issueAttachedLabels))
            .collectList()
            .block();

        StepVerifier.create(this.sut.findByRepoIdAndAttachedLabelsLabelId(
            this.repoId, this.labelIds.get(0), PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "issueNo"))))
            .assertNext(actual -> assertThat(actual.getIssueNo()).isEqualTo(3))
            .assertNext(actual -> assertThat(actual.getIssueNo()).isEqualTo(1))
            .expectNextCount(0)
            .verifyComplete();
    }


    private Mono<Void> changeContent(UUID id, IssueContent content, TransactionalOperator operator) {
        return this.sut.findById(id)
            .doOnNext(repo -> repo.changeContent(content))
            .flatMap(repo -> this.sut.save(repo))
            .as(operator::transactional)
            .then();
    }
}
