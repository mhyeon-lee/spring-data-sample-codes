package com.navercorp.spring.sql.groovy.repo;

import com.navercorp.spring.sql.groovy.account.Account;
import lombok.*;
import org.springframework.core.Ordered;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.conversion.MutableAggregateChange;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.event.BeforeSaveCallback;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;
import java.util.UUID;

@Table
@Getter
@EqualsAndHashCode(of = "id")
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__(@PersistenceConstructor))
@ToString
public class Repo {
    @Id
    private String id;

    private String name;

    private String description;

    private AggregateReference<Account, UUID> createdBy;

    private Instant createdAt;

    public Repo(String name, String description, AggregateReference<Account, UUID> createdBy) {
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.createdAt = Instant.now();
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changeDescription(String description) {
        this.description = description;
    }

    public static class RepoBeforeSaveCallback implements BeforeSaveCallback<Repo>, Ordered {
        private static final DateTimeFormatter ID_PREFIX_FORMAT = DateTimeFormatter
            .ofPattern("yyyyMMddHHmmss")
            .withZone(ZoneId.of("Asia/Seoul"));

        private static String generateId(Repo repo) {
            return new StringJoiner("-")
                .add(ID_PREFIX_FORMAT.format(repo.getCreatedAt()))
                .add(repo.getName())
                .toString();
        }

        @Override
        public Repo onBeforeSave(Repo aggregate, MutableAggregateChange<Repo> aggregateChange) {
            if (aggregate.id == null) {
                aggregate.id = generateId(aggregate);
            }
            return aggregate;
        }

        @Override
        public int getOrder() {
            return Ordered.LOWEST_PRECEDENCE;
        }
    }
}
