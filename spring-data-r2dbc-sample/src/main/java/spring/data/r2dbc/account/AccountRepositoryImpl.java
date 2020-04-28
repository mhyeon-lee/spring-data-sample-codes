package spring.data.r2dbc.account;

import org.reactivestreams.Publisher;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.ReactiveDataAccessStrategy;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class AccountRepositoryImpl implements AccountRepositoryCustom {
    private final R2dbcEntityOperations entityOperations;
    private final RelationalPersistentProperty idProperty;

    public AccountRepositoryImpl(R2dbcEntityOperations entityOperations, ReactiveDataAccessStrategy dataAccessStrategy) {
        this.entityOperations = entityOperations;
        this.idProperty = dataAccessStrategy.getConverter().getMappingContext()
            .getRequiredPersistentEntity(Account.class)
            .getRequiredIdProperty();
    }

    @Transactional
    public Mono<Void> deleteById(UUID id) {
        return this.entityOperations.selectOne(this.getIdQuery(id), Account.class)
            .switchIfEmpty(Mono.error(new TransientDataAccessResourceException("account does not exist.id: " + id)))
            .doOnNext(Account::delete)
            .flatMap(this.entityOperations::update)
            .then();
    }

    @Transactional
    public Mono<Void> deleteById(Publisher<UUID> id) {
        return Mono.from(id).flatMap(this::deleteById);
    }

    @Transactional
    public Mono<Void> delete(Account entity) {
        entity.delete();
        return this.entityOperations.update(entity).then();
    }

    @Transactional
    public Mono<Void> deleteAll(Iterable<? extends Account> entities) {
        return this.deleteAll(Flux.fromIterable(entities));
    }

    @Transactional
    public Mono<Void> deleteAll(Publisher<? extends Account> entityStream) {
        return Flux.from(entityStream)
            .flatMap(this::delete)
            .then();
    }

    @Transactional
    public Mono<Void> deleteAll() {
        return this.entityOperations.select(Account.class).all()
            .flatMap(this::delete)
            .then();
    }

    private Query getIdQuery(UUID id) {
        return Query.query(Criteria.where(this.idProperty.getName()).is(id));
    }
}
