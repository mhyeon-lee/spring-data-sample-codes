package spring.data.r2dbc.issue;

import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface IssueRepositoryCustom {
    Flux<Issue> findByRepoIdAndAttachedLabelsLabelId(String repoId, UUID labelId, Pageable pageable);
}
