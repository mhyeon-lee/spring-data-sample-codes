package spring.data.jpa.account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByIdAndStateIn(UUID uuid, Set<AccountState> states);

    default Optional<Account> findByIdExcludeDeleted(UUID id) {
        return this.findByIdAndStateIn(id, EnumSet.of(AccountState.ACTIVE, AccountState.LOCKED));
    }
}
