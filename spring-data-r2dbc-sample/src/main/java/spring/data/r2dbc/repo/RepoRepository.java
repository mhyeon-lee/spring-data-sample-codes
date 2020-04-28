package spring.data.r2dbc.repo;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Mono;
import spring.data.r2dbc.support.WithInsert;

public interface RepoRepository extends R2dbcRepository<Repo, String>, WithInsert<Repo> {
    @Query("SELECT * FROM REPO repo WHERE repo.id = :id FOR UPDATE")
    @Override
    Mono<Repo> findById(@Param("id") String id);
}
