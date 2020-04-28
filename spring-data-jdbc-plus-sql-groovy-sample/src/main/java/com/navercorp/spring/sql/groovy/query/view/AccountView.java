package com.navercorp.spring.sql.groovy.query.view;

import com.navercorp.spring.sql.groovy.account.AccountState;
import com.navercorp.spring.sql.groovy.support.EncryptString;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("ACCOUNT")
@Value
public class AccountView {
    @Id
    UUID id;

    String loginId;

    String name;

    AccountState state;

    EncryptString email;
}
