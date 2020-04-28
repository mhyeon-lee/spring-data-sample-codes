package spring.data.jdbc.repo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.transaction.support.TransactionTemplate;
import spring.data.jdbc.account.Account;
import spring.data.jdbc.test.DataInitializeExecutionListener;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestExecutionListeners(
    listeners = DataInitializeExecutionListener.class,
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class RepoRepositoryTest {
    @Autowired
    private RepoRepository sut;

    private final AggregateReference<Account, UUID> creatorId = AggregateReference.to(UUID.randomUUID());
    private final List<Repo> repos = List.of(
        new Repo("navercorp", "navercorp desc", this.creatorId),
        new Repo("spring-data-jdbc", "spring-data-jdbc desc", this.creatorId)
    );

    @Test
    void insert() {
        // given
        Repo repo = this.repos.get(0);

        // when
        Repo actual = this.sut.save(repo);

        // then
        assertThat(repo).isSameAs(actual);
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getId()).contains("-" + actual.getName());
        assertThat(actual.getCreatedBy()).isEqualTo(this.creatorId);
    }

    @Test
    void updateWithLock(@Autowired TransactionTemplate transactionTemplate) {
        // given
        Repo repo = this.repos.get(1);
        this.sut.save(repo);

        // when
        CompletableFuture<Void> future = transactionTemplate.execute(status -> {
            Repo loadWithLock = this.sut.findById(repo.getId()).get();

            // 비동기로 먼저 변경을 실행시키지만, LOCK 이 잡혀서 현재 트랜잭션이 종료될 때까지 대기한다.
            CompletableFuture<Void> futureChangeName =
                this.asyncChangeName(loadWithLock.getId(), "spring-data-jpa", transactionTemplate);

            this.sleep(1000);

            loadWithLock.changeName("spring-data-r2dbc");
            this.sut.save(loadWithLock);

            return futureChangeName;
        });

        // UPDATE 처리 작업을 기다린다.
        future.join();

        // when
        Optional<Repo> actual = this.sut.findById(repo.getId());

        // then
        assertThat(actual.get().getName()).isEqualTo("spring-data-jpa");
        assertThat(actual.get().getDescription()).isEqualTo("spring-data-jdbc desc");
    }

    @Test
    void updateDifferentPropertyWithLock(@Autowired TransactionTemplate transactionTemplate) {
        // given
        Repo repo = this.repos.get(1);
        this.sut.save(repo);

        // when
        CompletableFuture<Void> future = transactionTemplate.execute(status -> {
            Repo loadWithLock = this.sut.findById(repo.getId()).get();

            // 비동기로 먼저 변경을 실행시키지만, LOCK 이 잡혀서 현재 트랜잭션이 종료될 때까지 대기한다.
            // LOCK 을 잡은 트랜잭션이 description 수정 커밋 완료 후에 SELECT 하므로, 수정된 Description 값이 유지될 수 있다.
            CompletableFuture<Void> futureChangeName =
                this.asyncChangeName(loadWithLock.getId(), "spring-data-jpa", transactionTemplate);

            this.sleep(1000);

            loadWithLock.changeDescription("spring-data-r2dbc desc");
            this.sut.save(loadWithLock);

            return futureChangeName;
        });

        // UPDATE 처리 작업을 기다린다.
        future.join();

        // when
        Optional<Repo> actual = this.sut.findById(repo.getId());

        // then
        assertThat(actual.get().getName()).isEqualTo("spring-data-jpa");
        assertThat(actual.get().getDescription()).isEqualTo("spring-data-r2dbc desc");
    }

    private CompletableFuture<Void> asyncChangeName(String id, String name, TransactionTemplate transactionTemplate) {
        return CompletableFuture.runAsync(() ->
            transactionTemplate.execute(status -> {
                Repo loadWithLock = this.sut.findById(id).get();
                loadWithLock.changeName(name);
                this.sut.save(loadWithLock);
                return null;
            }));
    }

    private void sleep(long sleepMs) {
        try {
            Thread.sleep(sleepMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
