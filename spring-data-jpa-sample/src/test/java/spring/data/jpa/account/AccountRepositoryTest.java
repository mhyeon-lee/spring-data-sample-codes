package spring.data.jpa.account;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.transaction.support.TransactionTemplate;
import spring.data.jpa.test.DataInitializeExecutionListener;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestExecutionListeners(
    listeners = DataInitializeExecutionListener.class,
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class AccountRepositoryTest {
    @Autowired
    private AccountRepository sut;

    private final List<Account> accounts = List.of(
        Account.builder()
            .id(UUID.randomUUID())
            .loginId("navercorp.com")
            .name("naver")
            .state(AccountState.ACTIVE)
            .email("naver@navercorp.com")
            .build()
    );

    @Test
    void encryptDecrypt() {
        // given
        Account account = accounts.get(0);
        this.sut.save(account);

        // when
        Optional<Account> actual = this.sut.findById(account.getId());

        // then
        assertThat(actual.get().getEmail()).isEqualTo("naver@navercorp.com");
    }

    @Test
    void softDelete(@Autowired TransactionTemplate transactionTemplate) {
        // given
        Account account = accounts.get(0);
        this.sut.save(account);

        // when
        Account actual = transactionTemplate.execute(status -> {
            Account load = this.sut.findById(account.getId()).get();
            this.sut.delete(load);
            return load;
        });

        // then
        assertThat(actual.getState()).isEqualTo(AccountState.DELETED);

        Optional<Account> loadDeleted = this.sut.findById(actual.getId());
        assertThat(loadDeleted).isPresent();
        assertThat(loadDeleted.get().getState()).isEqualTo(AccountState.DELETED);

        Optional<Account> deleted = this.sut.findByIdExcludeDeleted(actual.getId());
        assertThat(deleted).isEmpty();
    }
}
