package com.navercorp.spring.sql.groovy.query.view;

import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("REPO")
@Value
public class IssueRepoView {
    @Id
    String id;

    String name;

    String description;
}
