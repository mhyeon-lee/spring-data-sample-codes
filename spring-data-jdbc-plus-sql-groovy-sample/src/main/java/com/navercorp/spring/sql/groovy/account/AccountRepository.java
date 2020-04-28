package com.navercorp.spring.sql.groovy.account;

import com.navercorp.spring.data.jdbc.plus.repository.JdbcRepository;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface AccountRepository extends JdbcRepository<Account, UUID>, AccountRepositoryCustom {
    @Override
    void deleteById(UUID id);

    @Override
    void delete(Account entity);

    @Override
    void deleteAll(Iterable<? extends Account> entities);

    @Override
    void deleteAll();

    Optional<Account> findByIdAndStateIn(UUID uuid, Set<AccountState> states);

    default Optional<Account> findByIdExcludeDeleted(UUID id) {
        return this.findByIdAndStateIn(id, EnumSet.of(AccountState.ACTIVE, AccountState.LOCKED));
    }
}
