package spring.data.jpa.issue;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IssueRepository extends JpaRepository<Issue, UUID> {
    Page<Issue> findByTitleLikeAndStatus(String titleStartAt, Status status, Pageable pageable);

    Page<Issue> findByRepoIdAndAttachedLabelsLabelId(String repoId, UUID labelId, Pageable pageable);
}
