package spring.data.r2dbc.issue;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.convert.MappingR2dbcConverter;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.data.r2dbc.mapping.R2dbcMappingContext;
import org.springframework.data.r2dbc.query.QueryMapper;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import reactor.core.publisher.Flux;
import spring.data.r2dbc.issue.sql.IssueSql;

import java.util.UUID;

public class IssueRepositoryImpl implements IssueRepositoryCustom {
    private final DatabaseClient dbClient;
    private final IssueSql issueSql;

    @SuppressWarnings("unchecked")
    public IssueRepositoryImpl(
        DatabaseClient dbClient,
        R2dbcMappingContext mappingContext,
        R2dbcDialect dialect,
        R2dbcCustomConversions conversions) {

        this.dbClient = dbClient;
        QueryMapper queryMapper = new QueryMapper(dialect, new MappingR2dbcConverter(mappingContext, conversions));
        this.issueSql = new IssueSql(
            (RelationalPersistentEntity<Issue>) mappingContext.getRequiredPersistentEntity(Issue.class),
            queryMapper);
    }

    @Override
    public Flux<Issue> findByRepoIdAndAttachedLabelsLabelId(String repoId, UUID labelId, Pageable pageable) {
        return this.dbClient.execute(this.issueSql.selectByRepoIdAndAttachedLabelsLabelId(pageable.getSort()))
            .as(Issue.class)
            .bind("repoId", repoId)
            .bind("labelId", labelId)
            .bind("offset", pageable.getOffset())
            .bind("pageSize", pageable.getPageSize())
            .fetch()
            .all();
    }
}
