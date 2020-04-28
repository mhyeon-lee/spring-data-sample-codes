package spring.data.r2dbc.issue;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface IssueRepository extends R2dbcRepository<Issue, UUID>, IssueRepositoryCustom {
    Flux<Issue> findByTitleLikeAndStatus(String titleStartAt, Status status, Pageable pageable);

    @Query("SELECT COUNT(*) FROM ISSUE WHERE title LIKE :titleStartAt AND status = :status")
    Mono<Long> countByTitleLikeAndStatus(@Param("titleStartAt") String titleStartAt, @Param("status") Status status);
}
