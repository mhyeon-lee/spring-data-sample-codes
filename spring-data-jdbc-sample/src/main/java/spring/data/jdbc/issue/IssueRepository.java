package spring.data.jdbc.issue;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface IssueRepository extends PagingAndSortingRepository<Issue, UUID>, IssueRepositoryCustom {
    List<Issue> findByTitleLikeAndStatus(String titleStartAt, Status status, Pageable pageable);

    @Query("SELECT COUNT(*) FROM ISSUE WHERE title LIKE :titleStartAt AND status = :status")
    long countByTitleLikeAndStatus(@Param("titleStartAt") String titleStartAt, @Param("status") Status status);

    @Modifying
    @Query("UPDATE ISSUE SET VERSION = VERSION + 1, STATUS = :status WHERE ID = :id")
    boolean changeStatus(@Param("id") UUID id, @Param("status") Status status);
}
