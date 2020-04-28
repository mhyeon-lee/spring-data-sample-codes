package spring.data.r2dbc.repo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import spring.data.r2dbc.test.DataInitializeExecutionListener;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestExecutionListeners(
    listeners = DataInitializeExecutionListener.class,
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class RepoRepositoryTest {
    @Autowired
    private RepoRepository sut;

    private final UUID creatorId = UUID.randomUUID();
    private final List<Repo> repos = List.of(
        new Repo("navercorp", "navercorp desc", this.creatorId),
        new Repo("spring-data-jdbc", "spring-data-jdbc desc",  this.creatorId)
    );

    @Test
    void insert() {
        Repo repo = this.repos.get(0);

        StepVerifier.create(this.sut.insert(repo))
            .assertNext(actual -> {
                assertThat(repo).isSameAs(actual);
                assertThat(actual.getId()).isNotNull();
                assertThat(actual.getId()).contains("-" + actual.getName());
            })
            .expectNextCount(0)
            .verifyComplete();
    }

    @Test
    void updateWithLock(@Autowired TransactionalOperator operator) {
        Repo repo = this.repos.get(1);
        this.sut.insert(repo).block();

        StepVerifier.create(
            this.sut.findById(repo.getId())
                .doOnNext(actual ->
                    // 비동기로 먼저 변경을 실행시키지만, LOCK 이 잡혀서 현재 트랜잭션이 종료될 때까지 대기한다.
                    this.changeName(repo.getId(), "spring-data-jpa", operator)
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe()
                )
                .delayElement(Duration.ofSeconds(1))
                .doOnNext(actual -> actual.changeName("spring-data-r2dbc"))
                .flatMap(actual -> this.sut.save(actual))
                .as(operator::transactional)
                .then())
            .verifyComplete();

        StepVerifier.create(this.sut.findById(repo.getId()))
            .assertNext(actual -> {
                assertThat(actual.getName()).isEqualTo("spring-data-jpa");
                assertThat(actual.getDescription()).isEqualTo("spring-data-jdbc desc");
            })
            .expectNextCount(0)
            .verifyComplete();
    }

    @Test
    void updateDifferentPropertyWithLock(@Autowired TransactionalOperator operator) {
        Repo repo = this.repos.get(1);
        this.sut.insert(repo).block();

        StepVerifier.create(
            this.sut.findById(repo.getId())
                .doOnNext(actual ->
                    // 비동기로 먼저 변경을 실행시키지만, LOCK 이 잡혀서 현재 트랜잭션이 종료될 때까지 대기한다.
                    this.changeName(repo.getId(), "spring-data-jpa", operator)
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe()
                )
                .delayElement(Duration.ofSeconds(1))
                .doOnNext(actual -> actual.changeDescription("spring-data-r2dbc desc"))
                .flatMap(actual -> this.sut.save(actual))
                .as(operator::transactional)
                .then())
            .verifyComplete();

        StepVerifier.create(this.sut.findById(repo.getId()))
            .assertNext(actual -> {
                assertThat(actual.getName()).isEqualTo("spring-data-jpa");
                assertThat(actual.getDescription()).isEqualTo("spring-data-r2dbc desc");
            })
            .expectNextCount(0)
            .verifyComplete();
    }

    private Mono<Void> changeName(String id, String name, TransactionalOperator operator) {
        return this.sut.findById(id)
            .doOnNext(repo -> repo.changeName(name))
            .flatMap(repo -> this.sut.save(repo))
            .as(operator::transactional)
            .then();
    }
}
