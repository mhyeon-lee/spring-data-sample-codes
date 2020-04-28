package spring.data.r2dbc.account;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import reactor.test.StepVerifier;
import spring.data.r2dbc.support.EncryptString;
import spring.data.r2dbc.test.DataInitializeExecutionListener;

import javax.validation.ConstraintViolationException;
import java.util.List;
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
        Account account = this.accounts.get(0);

        StepVerifier.create(this.sut.insert(account))
            .assertNext(actual -> assertThat(account).isSameAs(actual))
            .expectNextCount(0)
            .verifyComplete();
    }

    @Test
    void insertInvalid() {
        Account account = Account.builder()
            .id(UUID.randomUUID())
            .loginId(" ")
            .name("naver")
            .state(AccountState.ACTIVE)
            .email(new EncryptString("naver@navercorp.com"))
            .build();

        StepVerifier.create(this.sut.insert(account))
            .expectError(ConstraintViolationException.class)
            .verify();
    }

    @Test
    void encryptDecrypt() {
        Account account = this.accounts.get(0);
        this.sut.insert(account).block();

        StepVerifier.create(this.sut.findById(account.getId()))
            .assertNext(actual -> assertThat(actual.getEmail().getValue()).isEqualTo("naver@navercorp.com"))
            .expectNextCount(0)
            .verifyComplete();
    }

    @Test
    void softDelete() {
        Account account = this.accounts.get(1);
        this.sut.insert(account).block();

        StepVerifier.create(this.sut.delete(account))
            .expectNextCount(0)
            .verifyComplete();

        assertThat(account.getState()).isEqualTo(AccountState.DELETED);

        StepVerifier.create(this.sut.findById(account.getId()))
            .assertNext(actual -> assertThat(actual.getState()).isEqualTo(AccountState.DELETED))
            .expectNextCount(0)
            .verifyComplete();

        StepVerifier.create(this.sut.findByIdExcludeDeleted(account.getId()))
            .expectNextCount(0)
            .verifyComplete();
    }
}
