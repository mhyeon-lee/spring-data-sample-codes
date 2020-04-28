package spring.data.r2dbc.account;

import org.reactivestreams.Publisher;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;
import spring.data.r2dbc.support.WithInsert;

import java.util.UUID;

public interface AccountRepository extends R2dbcRepository<Account, UUID>, AccountRepositoryCustom, WithInsert<Account> {
    @Override
    Mono<Void> deleteById(UUID id);

    @Override
    Mono<Void> deleteById(Publisher<UUID> id);

    @Override
    Mono<Void> delete(Account entity);

    @Override
    Mono<Void> deleteAll(Iterable<? extends Account> entities);

    @Override
    Mono<Void> deleteAll(Publisher<? extends Account> entityStream);

    @Override
    Mono<Void> deleteAll();

    Mono<Account> findByIdAndStateNot(UUID uuid, AccountState excludeState);

    default Mono<Account> findByIdExcludeDeleted(UUID id) {
        return this.findByIdAndStateNot(id, AccountState.DELETED);
    }
}
