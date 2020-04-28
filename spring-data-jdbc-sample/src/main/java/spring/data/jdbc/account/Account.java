package spring.data.jdbc.account;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import spring.data.jdbc.support.EncryptString;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

@Builder
@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class Account {
    @Id
    private UUID id;

    @NotBlank
    @Size(max = 50)
    private String loginId;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    private AccountState state;

    @Valid
    private EncryptString email;

    @NotNull
    @PastOrPresent
    @Builder.Default
    private Instant createdAt = Instant.now();

    public void lock() {
        this.state = AccountState.LOCKED;
    }

    public void delete() {
        this.state = AccountState.DELETED;
    }
}
