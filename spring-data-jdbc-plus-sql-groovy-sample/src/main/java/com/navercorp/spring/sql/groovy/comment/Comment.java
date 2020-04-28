package com.navercorp.spring.sql.groovy.comment;

import com.navercorp.spring.sql.groovy.account.Account;
import com.navercorp.spring.sql.groovy.issue.Issue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table
@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class Comment {
    @Id
    private Long id;

    @Version
    private long version;

    private AggregateReference<Issue, UUID> issueId;

    @Column("ID") // PK MAPPING, default: "COMMENT"
    private CommentContent content;

    private AggregateReference<Account, UUID> createdBy;

    private Instant createdAt;

    public Comment(
        AggregateReference<Issue, UUID> issueId,
        CommentContent content,
        AggregateReference<Account, UUID> createdBy) {

        this.issueId = issueId;
        this.content = content;
        this.createdBy = createdBy;
        this.createdAt = Instant.now();
    }
}
