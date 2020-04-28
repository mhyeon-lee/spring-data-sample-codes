package com.navercorp.spring.sql.groovy.account;

import com.navercorp.spring.sql.groovy.support.EncryptString;
import com.navercorp.spring.sql.groovy.test.DataInitializeExecutionListener;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;

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
            .email(new EncryptString("naver@navercorp.com"))
            .build(),
        Account.builder()
            .id(UUID.randomUUID())
            .loginId("mhyeon.lee")
            .name("Myeonghyeon Lee")
            .state(AccountState.ACTIVE)
            .email(new EncryptString("mhyeon.lee@navercorp.com"))
            .build()
    );

    @Test
    void insert() {
        // given
        Account account = this.accounts.get(0);

        // when
        Account actual = this.sut.insert(account);

        // then
        assertThat(account).isSameAs(actual);
    }

    @Test
    void encryptDecrypt() {
        // given
        Account account = this.accounts.get(0);
        this.sut.insert(account);

        // when
        Optional<Account> actual = this.sut.findById(account.getId());

        // then
        assertThat(actual.get().getEmail().getValue()).isEqualTo("naver@navercorp.com");
    }

    @Test
    void softDelete() {
        // given
        Account account = this.accounts.get(1);
        account = this.sut.insert(account);

        // when
        this.sut.delete(account);

        // then
        assertThat(account.getState()).isEqualTo(AccountState.DELETED);

        Optional<Account> load = this.sut.findById(account.getId());
        assertThat(load).isPresent();
        assertThat(load.get().getState()).isEqualTo(AccountState.DELETED);

        Optional<Account> exclude = this.sut.findByIdExcludeDeleted(account.getId());
        assertThat(exclude).isEmpty();
    }
}
