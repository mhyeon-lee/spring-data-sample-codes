package com.navercorp.spring.sql.groovy.query.grid;

import com.navercorp.spring.sql.groovy.comment.CommentContent;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("COMMENT")
@Value
public class CommentGrid {
    @Id
    Long id;

    CommentContent content;

    AccountGrid creator;

    Instant createdAt;
}
