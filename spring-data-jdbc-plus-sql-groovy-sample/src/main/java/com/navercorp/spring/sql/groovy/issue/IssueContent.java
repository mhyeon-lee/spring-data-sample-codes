package com.navercorp.spring.sql.groovy.issue;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__(@PersistenceConstructor))
public class IssueContent {
    @Id
    @With
    Long id;

    String body;

    String mimeType;

    public IssueContent(String body, String mimeType) {
        this.id = null;
        this.body = body;
        this.mimeType = mimeType;
    }
}
