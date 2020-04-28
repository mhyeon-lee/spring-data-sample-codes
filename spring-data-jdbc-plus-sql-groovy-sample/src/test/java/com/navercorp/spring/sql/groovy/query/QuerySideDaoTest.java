package com.navercorp.spring.sql.groovy.query;

import com.navercorp.spring.sql.groovy.account.Account;
import com.navercorp.spring.sql.groovy.account.AccountRepository;
import com.navercorp.spring.sql.groovy.account.AccountState;
import com.navercorp.spring.sql.groovy.comment.Comment;
import com.navercorp.spring.sql.groovy.comment.CommentContent;
import com.navercorp.spring.sql.groovy.comment.CommentRepository;
import com.navercorp.spring.sql.groovy.issue.*;
import com.navercorp.spring.sql.groovy.label.Label;
import com.navercorp.spring.sql.groovy.label.LabelRepository;
import com.navercorp.spring.sql.groovy.query.criteria.IssueGridCriteria;
import com.navercorp.spring.sql.groovy.query.criteria.IssueViewCriteria;
import com.navercorp.spring.sql.groovy.query.grid.IssueGrid;
import com.navercorp.spring.sql.groovy.query.view.IssueView;
import com.navercorp.spring.sql.groovy.repo.Repo;
import com.navercorp.spring.sql.groovy.repo.RepoRepository;
import com.navercorp.spring.sql.groovy.support.EncryptString;
import com.navercorp.spring.sql.groovy.test.DataInitializeExecutionListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.util.StreamUtils;
import org.springframework.test.context.TestExecutionListeners;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestExecutionListeners(
    listeners = DataInitializeExecutionListener.class,
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class QuerySideDaoTest {
    @Autowired
    private QuerySideDao sut;

    private List<Account> accounts;
    private List<Repo> repos;
    private List<Label> labels;
    private List<Issue> issues;
    private List<Comment> comments;

    @BeforeEach
    void setUp(
        @Autowired AccountRepository accountRepository,
        @Autowired RepoRepository repoRepository,
        @Autowired LabelRepository labelRepository,
        @Autowired IssueRepository issueRepository,
        @Autowired CommentRepository commentRepository
    ) {
        this.accounts = StreamUtils.createStreamFromIterator(
            accountRepository.insertAll(
                List.of(
                    Account.builder()
                        .id(UUID.randomUUID())
                        .loginId("navercorp.com")
                        .name("naver")
                        .state(AccountState.ACTIVE)
                        .email(new EncryptString("naver@navercorp.com"))
                        .build(),
                    Account.builder()
                        .id(UUID.randomUUID())
                        .loginId("mhyeon.lee")
                        .name("Myeonghyeon Lee")
                        .state(AccountState.ACTIVE)
                        .email(new EncryptString("mhyeon.lee@navercorp.com"))
                        .build()
                )
            ).iterator())
            .collect(toList());

        this.repos = StreamUtils.createStreamFromIterator(
            repoRepository.saveAll(
                List.of(
                    new Repo("navercorp", "navercorp desc", AggregateReference.to(this.accounts.get(0).getId())),
                    new Repo("spring-data-jdbc", "spring-data-jdbc desc", AggregateReference.to(this.accounts.get(0).getId()))
                )
            ).iterator())
            .collect(toList());

        this.labels = StreamUtils.createStreamFromIterator(
            labelRepository.saveAll(
                List.of(
                    new Label(AggregateReference.to(this.repos.get(0).getId()), "bug", "red"),
                    new Label(AggregateReference.to(this.repos.get(0).getId()), "feature", "blue"),
                    new Label(AggregateReference.to(this.repos.get(0).getId()), "release", "black")
                )
            ).iterator())
            .collect(toList());

        this.issues = StreamUtils.createStreamFromIterator(
            issueRepository.saveAll(
                List.of(
                    Issue.builder()
                        .id(UUID.randomUUID())
                        .version(0L)
                        .repoId(AggregateReference.to(this.repos.get(0).getId()))
                        .issueNo(1L)
                        .status(Status.OPEN)
                        .title("issue 1")
                        .content(new IssueContent("content 1", "text/plain"))
                        .attachedLabels(new ArrayList<>(List.of(
                            IssueAttachedLabel.builder()
                                .labelId(AggregateReference.to(this.labels.get(0).getId()))
                                .attachedAt(Instant.now().minus(3L, ChronoUnit.DAYS))
                                .build(),
                            IssueAttachedLabel.builder()
                                .labelId(AggregateReference.to(this.labels.get(1).getId()))
                                .attachedAt(Instant.now().minus(2L, ChronoUnit.DAYS))
                                .build()
                        )))
                        .createdBy(AggregateReference.to(this.accounts.get(0).getId()))
                        .build(),
                    Issue.builder()
                        .id(UUID.randomUUID())
                        .version(0L)
                        .repoId(AggregateReference.to(this.repos.get(0).getId()))
                        .issueNo(2L)
                        .status(Status.OPEN)
                        .title("issue 2")
                        .content(new IssueContent("content 2", "text/plain"))
                        .attachedLabels(new ArrayList<>(List.of(
                            IssueAttachedLabel.builder()
                                .labelId(AggregateReference.to(this.labels.get(1).getId()))
                                .attachedAt(Instant.now().minus(3L, ChronoUnit.DAYS))
                                .build(),
                            IssueAttachedLabel.builder()
                                .labelId(AggregateReference.to(this.labels.get(2).getId()))
                                .attachedAt(Instant.now().minus(2L, ChronoUnit.DAYS))
                                .build()
                        )))
                        .createdBy(AggregateReference.to(this.accounts.get(0).getId()))
                        .build(),
                    Issue.builder()
                        .id(UUID.randomUUID())
                        .version(0L)
                        .repoId(AggregateReference.to(this.repos.get(0).getId()))
                        .issueNo(3L)
                        .status(Status.CLOSED)
                        .title("issue 3")
                        .content(new IssueContent("content 3", "text/plain"))
                        .attachedLabels(new ArrayList<>(List.of(
                            IssueAttachedLabel.builder()
                                .labelId(AggregateReference.to(this.labels.get(0).getId()))
                                .attachedAt(Instant.now().minus(3L, ChronoUnit.DAYS))
                                .build(),
                            IssueAttachedLabel.builder()
                                .labelId(AggregateReference.to(this.labels.get(2).getId()))
                                .attachedAt(Instant.now().minus(2L, ChronoUnit.DAYS))
                                .build()
                        )))
                        .createdBy(AggregateReference.to(this.accounts.get(0).getId()))
                        .build()
                )).iterator())
            .collect(toList());

        this.comments = StreamUtils.createStreamFromIterator(
            commentRepository.saveAll(
                List.of(
                    new Comment(
                        AggregateReference.to(this.issues.get(0).getId()),
                        new CommentContent("comment 1", "text/plain"),
                        AggregateReference.to(this.accounts.get(0).getId())),
                    new Comment(
                        AggregateReference.to(this.issues.get(0).getId()),
                        new CommentContent("comment 2", "text/plain"),
                        AggregateReference.to(this.accounts.get(0).getId()))
                )
            ).iterator())
            .collect(toList());
    }

    @Test
    void selectIssueGrids() {
        // given
        IssueGridCriteria criteria = IssueGridCriteria.builder()
            .status(Status.OPEN)
            .createdBy(AggregateReference.to(this.accounts.get(0).getId()))
            .searchRepoName("%naver%")
            .searchContent("%ssu%")
            .build();
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Order.desc("ISSUE_NO"), Sort.Order.asc("comment.CREATED_AT")));

        // when
        Page<IssueGrid> actual = this.sut.selectIssueGrid(criteria, pageable);

        // then
        assertThat(actual).hasSize(2);
        assertThat(actual.getTotalPages()).isEqualTo(2);
        assertThat(actual.getTotalElements()).isEqualTo(3);

        IssueGrid issue1 = actual.getContent().get(0);
        assertThat(issue1.getId()).isEqualTo(this.issues.get(1).getId());
        assertThat(issue1.getIssueNo()).isEqualTo(this.issues.get(1).getIssueNo());
        assertThat(issue1.getTitle()).isEqualTo(this.issues.get(1).getTitle());
        assertThat(issue1.getContent().getBody()).isEqualTo(this.issues.get(1).getContent().getBody());
        assertThat(issue1.getContent().getMimeType()).isEqualTo(this.issues.get(1).getContent().getMimeType());
        assertThat(issue1.getCreator().getName()).isEqualTo(this.accounts.get(0).getName());
        assertThat(issue1.getRepo().getId()).isEqualTo(this.repos.get(0).getId());
        assertThat(issue1.getRepo().getName()).isEqualTo(this.repos.get(0).getName());

        IssueGrid issue2 = actual.getContent().get(1);
        assertThat(issue2.getId()).isEqualTo(this.issues.get(0).getId());
        assertThat(issue2.getIssueNo()).isEqualTo(this.issues.get(0).getIssueNo());
        assertThat(issue2.getTitle()).isEqualTo(this.issues.get(0).getTitle());
        assertThat(issue2.getContent().getBody()).isEqualTo(this.issues.get(0).getContent().getBody());
        assertThat(issue2.getContent().getMimeType()).isEqualTo(this.issues.get(0).getContent().getMimeType());
        assertThat(issue2.getCreator().getName()).isEqualTo(this.accounts.get(0).getName());
        assertThat(issue2.getRepo().getId()).isEqualTo(this.repos.get(0).getId());
        assertThat(issue2.getRepo().getName()).isEqualTo(this.repos.get(0).getName());
        assertThat(issue2.getComment().getId()).isNotNull();
    }

    @Test
    void selectIssueView() {
        // given
        Issue issue = this.issues.get(0);
        UUID issueId = issue.getId();

        // when
        IssueView actual = this.sut.selectIssueView(issueId);

        // then
        assertThat(actual.getId()).isEqualTo(issueId);
        assertThat(actual.getVersion()).isEqualTo(issue.getVersion());
        assertThat(actual.getIssueNo()).isEqualTo(issue.getIssueNo());
        assertThat(actual.getTitle()).isEqualTo(issue.getTitle());
        assertThat(actual.getContent().getBody()).isEqualTo(issue.getContent().getBody());
        assertThat(actual.getContent().getMimeType()).isEqualTo(issue.getContent().getMimeType());
        assertThat(actual.getCreator().getId()).isEqualTo(this.accounts.get(0).getId());
        assertThat(actual.getCreator().getName()).isEqualTo(this.accounts.get(0).getName());
        assertThat(actual.getRepo().getId()).isEqualTo(this.repos.get(0).getId());
        assertThat(actual.getRepo().getName()).isEqualTo(this.repos.get(0).getName());
        assertThat(actual.getLabels()).hasSize(2);
        assertThat(actual.getLabels().get(0).getId()).isEqualTo(this.labels.get(0).getId());
        assertThat(actual.getLabels().get(0).getName()).isEqualTo(this.labels.get(0).getName());
        assertThat(actual.getLabels().get(1).getId()).isEqualTo(this.labels.get(1).getId());
        assertThat(actual.getLabels().get(1).getName()).isEqualTo(this.labels.get(1).getName());
        assertThat(actual.getComments()).hasSize(2);
        assertThat(actual.getComments().get(0).getId()).isEqualTo(this.comments.get(0).getId());
        assertThat(actual.getComments().get(0).getContent().getId()).isEqualTo(this.comments.get(0).getContent().getId());
        assertThat(actual.getComments().get(0).getContent().getBody()).isEqualTo(this.comments.get(0).getContent().getBody());
        assertThat(actual.getComments().get(0).getCreator().getId()).isEqualTo(this.accounts.get(0).getId());
        assertThat(actual.getComments().get(0).getCreator().getName()).isEqualTo(this.accounts.get(0).getName());
        assertThat(actual.getComments().get(1).getId()).isEqualTo(this.comments.get(1).getId());
        assertThat(actual.getComments().get(1).getContent().getId()).isEqualTo(this.comments.get(1).getContent().getId());
        assertThat(actual.getComments().get(1).getContent().getBody()).isEqualTo(this.comments.get(1).getContent().getBody());
        assertThat(actual.getComments().get(1).getCreator().getId()).isEqualTo(this.accounts.get(0).getId());
        assertThat(actual.getComments().get(1).getCreator().getName()).isEqualTo(this.accounts.get(0).getName());
    }

    @Test
    void selectIssueViews() {
        // given
        IssueViewCriteria criteria = IssueViewCriteria.builder()
            .status(Status.OPEN)
            .createdBy(AggregateReference.to(this.accounts.get(0).getId()))
            .labelIds(List.of(
                AggregateReference.to(this.labels.get(0).getId()),
                AggregateReference.to(this.labels.get(1).getId())
            ))
            .searchRepoName("%naver%")
            .searchContent("%ssu%")
            .build();
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "ISSUE_NO"));

        // when
        Page<IssueView> actual = this.sut.selectIssueViews(criteria, pageable);

        // then
        assertThat(actual).hasSize(2);
        assertThat(actual.getTotalPages()).isEqualTo(1);
        assertThat(actual.getTotalElements()).isEqualTo(2);

        IssueView issue1 = actual.getContent().get(0);
        assertThat(issue1.getId()).isEqualTo(this.issues.get(1).getId());
        assertThat(issue1.getVersion()).isEqualTo(this.issues.get(1).getVersion());
        assertThat(issue1.getIssueNo()).isEqualTo(this.issues.get(1).getIssueNo());
        assertThat(issue1.getTitle()).isEqualTo(this.issues.get(1).getTitle());
        assertThat(issue1.getContent().getBody()).isEqualTo(this.issues.get(1).getContent().getBody());
        assertThat(issue1.getContent().getMimeType()).isEqualTo(this.issues.get(1).getContent().getMimeType());
        assertThat(issue1.getCreator().getId()).isEqualTo(this.accounts.get(0).getId());
        assertThat(issue1.getCreator().getName()).isEqualTo(this.accounts.get(0).getName());
        assertThat(issue1.getRepo().getId()).isEqualTo(this.repos.get(0).getId());
        assertThat(issue1.getRepo().getName()).isEqualTo(this.repos.get(0).getName());
        assertThat(issue1.getLabels()).hasSize(2);
        assertThat(issue1.getLabels().get(0).getId()).isEqualTo(this.labels.get(1).getId());
        assertThat(issue1.getLabels().get(0).getName()).isEqualTo(this.labels.get(1).getName());
        assertThat(issue1.getLabels().get(1).getId()).isEqualTo(this.labels.get(2).getId());
        assertThat(issue1.getLabels().get(1).getName()).isEqualTo(this.labels.get(2).getName());
        assertThat(issue1.getComments()).hasSize(0);

        IssueView issue2 = actual.getContent().get(1);
        assertThat(issue2.getId()).isEqualTo(this.issues.get(0).getId());
        assertThat(issue2.getVersion()).isEqualTo(this.issues.get(0).getVersion());
        assertThat(issue2.getIssueNo()).isEqualTo(this.issues.get(0).getIssueNo());
        assertThat(issue2.getTitle()).isEqualTo(this.issues.get(0).getTitle());
        assertThat(issue2.getContent().getBody()).isEqualTo(this.issues.get(0).getContent().getBody());
        assertThat(issue2.getContent().getMimeType()).isEqualTo(this.issues.get(0).getContent().getMimeType());
        assertThat(issue2.getCreator().getId()).isEqualTo(this.accounts.get(0).getId());
        assertThat(issue2.getCreator().getName()).isEqualTo(this.accounts.get(0).getName());
        assertThat(issue2.getRepo().getId()).isEqualTo(this.repos.get(0).getId());
        assertThat(issue2.getRepo().getName()).isEqualTo(this.repos.get(0).getName());
        assertThat(issue2.getLabels()).hasSize(2);
        assertThat(issue2.getLabels().get(0).getId()).isEqualTo(this.labels.get(0).getId());
        assertThat(issue2.getLabels().get(0).getName()).isEqualTo(this.labels.get(0).getName());
        assertThat(issue2.getLabels().get(1).getId()).isEqualTo(this.labels.get(1).getId());
        assertThat(issue2.getLabels().get(1).getName()).isEqualTo(this.labels.get(1).getName());
        assertThat(issue2.getComments()).hasSize(2);
        assertThat(issue2.getComments().get(0).getId()).isEqualTo(this.comments.get(0).getId());
        assertThat(issue2.getComments().get(0).getContent().getId()).isEqualTo(this.comments.get(0).getContent().getId());
        assertThat(issue2.getComments().get(0).getContent().getBody()).isEqualTo(this.comments.get(0).getContent().getBody());
        assertThat(issue2.getComments().get(0).getCreator().getId()).isEqualTo(this.accounts.get(0).getId());
        assertThat(issue2.getComments().get(0).getCreator().getName()).isEqualTo(this.accounts.get(0).getName());
        assertThat(issue2.getComments().get(1).getId()).isEqualTo(this.comments.get(1).getId());
        assertThat(issue2.getComments().get(1).getContent().getId()).isEqualTo(this.comments.get(1).getContent().getId());
        assertThat(issue2.getComments().get(1).getContent().getBody()).isEqualTo(this.comments.get(1).getContent().getBody());
        assertThat(issue2.getComments().get(1).getCreator().getId()).isEqualTo(this.accounts.get(0).getId());
        assertThat(issue2.getComments().get(1).getCreator().getName()).isEqualTo(this.accounts.get(0).getName());
    }
}
