package com.navercorp.spring.sql.groovy.issue;

import com.navercorp.spring.sql.groovy.label.Label;
import com.navercorp.spring.sql.groovy.repo.Repo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

import java.util.UUID;

public interface IssueRepositoryCustom {
    Page<Issue> findByRepoIdAndAttachedLabelsLabelId(
        AggregateReference<Repo, String> repoId,
        AggregateReference<Label, UUID> labelId,
        Pageable pageable);
}
