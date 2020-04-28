package com.navercorp.spring.sql.groovy.issue;

import com.navercorp.spring.sql.groovy.account.Account;
import com.navercorp.spring.sql.groovy.repo.Repo;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Table
@Builder
@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class Issue {
    @Id
    private UUID id;

    @Version
    private long version;

    private AggregateReference<Repo, String> repoId;

    private Long issueNo;

    private Status status;

    private String title;

    @Column("ISSUE_ID") // default: "ISSUE"
    private IssueContent content;

    @MappedCollection(idColumn = "ISSUE_ID", keyColumn = "ATTACHED_AT")
    @Builder.Default
    private List<IssueAttachedLabel> attachedLabels = new ArrayList<>();

    private AggregateReference<Account, UUID> createdBy;

    @Builder.Default
    private Instant createdAt = Instant.now();

    public void changeContent(IssueContent content) {
        this.content = content;
    }

    public List<IssueAttachedLabel> getAttachedLabels() {
        return Collections.unmodifiableList(this.attachedLabels);
    }
}
