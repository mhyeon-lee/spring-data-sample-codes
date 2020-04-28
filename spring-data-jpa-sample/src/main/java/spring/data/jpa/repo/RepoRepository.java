package spring.data.jpa.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface RepoRepository extends JpaRepository<Repo, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Repo> findById(String id);
}
