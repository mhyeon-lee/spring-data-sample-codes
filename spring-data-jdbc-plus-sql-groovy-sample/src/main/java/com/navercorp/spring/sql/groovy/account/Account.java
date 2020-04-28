package com.navercorp.spring.sql.groovy.account;

import com.navercorp.spring.sql.groovy.support.EncryptString;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table
@Builder
@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class Account {
    @Id
    private UUID id;

    private String loginId;

    private String name;

    private AccountState state;

    private EncryptString email;

    @Builder.Default
    private Instant createdAt = Instant.now();

    public void lock() {
        this.state = AccountState.LOCKED;
    }

    public void delete() {
        this.state = AccountState.DELETED;
    }
}
