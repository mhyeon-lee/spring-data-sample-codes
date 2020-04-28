package com.navercorp.spring.sql.groovy.query.view;

import com.navercorp.spring.sql.groovy.issue.IssueContent;
import com.navercorp.spring.sql.groovy.issue.Status;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Table("ISSUE")
@Value
public class IssueView {
    @Id
    UUID id;

    long version;

    Long issueNo;

    Status status;

    String title;

    IssueContent content;

    AccountView creator;

    IssueRepoView repo;

    Instant createdAt;

    List<IssueLabelView> labels;

    List<CommentView> comments;
}
