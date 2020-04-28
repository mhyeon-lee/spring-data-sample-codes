package spring.data.jdbc.account;

import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class AccountRepositoryImpl implements AccountRepositoryCustom {
    private final JdbcAggregateOperations jdbcAggregateOperations;

    public AccountRepositoryImpl(JdbcAggregateOperations jdbcAggregateOperations) {
        this.jdbcAggregateOperations = jdbcAggregateOperations;
    }

    @Transactional
    public void deleteById(UUID id) {
        Account account = this.jdbcAggregateOperations.findById(id, Account.class);
        if (account == null) {
            throw new TransientDataAccessResourceException("account does not exist.id: " + id);
        }

        this.delete(account);
    }

    @Transactional
    public void delete(Account entity) {
        entity.delete();
        this.jdbcAggregateOperations.update(entity);
    }

    @Transactional
    public void deleteAll(Iterable<? extends Account> entities) {
        entities.forEach(this::delete);
    }

    @Transactional
    public void deleteAll() {
        Iterable<Account> accounts = this.jdbcAggregateOperations.findAll(Account.class);
        this.deleteAll(accounts);
    }
}
