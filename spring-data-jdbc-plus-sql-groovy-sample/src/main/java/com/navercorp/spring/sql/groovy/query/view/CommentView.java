package com.navercorp.spring.sql.groovy.query.view;

import com.navercorp.spring.sql.groovy.comment.CommentContent;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("COMMENT")
@Value
public class CommentView {
    @Id
    Long id;

    CommentContent content;

    AccountView creator;

    Instant createdAt;
}
