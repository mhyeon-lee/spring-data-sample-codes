package spring.data.jdbc.issue;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import spring.data.jdbc.label.Label;
import spring.data.jdbc.repo.Repo;

import java.util.UUID;

public interface IssueRepositoryCustom {
    Page<Issue> findByRepoIdAndAttachedLabelsLabelId(
        AggregateReference<Repo, String> repoId,
        AggregateReference<Label, UUID> labelId,
        Pageable pageable);
}
