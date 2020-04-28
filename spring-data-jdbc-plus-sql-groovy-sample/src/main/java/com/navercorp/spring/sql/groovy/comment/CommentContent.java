package com.navercorp.spring.sql.groovy.comment;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__(@PersistenceConstructor))
public class CommentContent {
    @Id
    @With
    Long id;

    String body;

    String mimeType;

    public CommentContent(String body, String mimeType) {
        this.id = null;
        this.body = body;
        this.mimeType = mimeType;
    }
}
