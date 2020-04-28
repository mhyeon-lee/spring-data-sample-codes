package spring.data.jpa.issue;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface IssueRepository extends JpaRepository<Issue, UUID> {
    Page<Issue> findByTitleLikeAndStatus(String titleStartAt, Status status, Pageable pageable);

    Page<Issue> findByRepoIdAndAttachedLabelsLabelId(String repoId, UUID labelId, Pageable pageable);

    @Transactional
    @Modifying
    @Query("UPDATE Issue i SET i.version = i.version + 1, i.status = :status WHERE i.id = :id")
    int changeStatus(@Param("id") UUID id, @Param("status") Status status);
}
