package com.navercorp.spring.sql.groovy.query.grid;

import com.navercorp.spring.sql.groovy.issue.IssueContent;
import com.navercorp.spring.sql.groovy.issue.Status;
import com.navercorp.spring.sql.groovy.query.view.CommentView;
import com.navercorp.spring.sql.groovy.query.view.IssueRepoView;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("ISSUE")
@Value
public class IssueGrid {
    @Id
    UUID id;

    Long issueNo;

    Status status;

    String title;

    IssueContent content;

    AccountGrid creator;

    IssueRepoView repo;

    Instant createdAt;

    CommentView comment;
}
