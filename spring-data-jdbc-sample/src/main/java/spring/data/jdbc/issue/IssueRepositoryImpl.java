package spring.data.jdbc.issue;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.core.convert.EntityRowMapper;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import spring.data.jdbc.issue.sql.IssueSql;
import spring.data.jdbc.label.Label;
import spring.data.jdbc.repo.Repo;

import java.util.List;
import java.util.UUID;

public class IssueRepositoryImpl implements IssueRepositoryCustom {
    private final NamedParameterJdbcOperations jdbcOperations;
    private final EntityRowMapper<Issue> rowMapper;

    @SuppressWarnings("unchecked")
    public IssueRepositoryImpl(
        NamedParameterJdbcOperations jdbcOperations,
        RelationalMappingContext mappingContext,
        JdbcConverter jdbcConverter) {

        this.jdbcOperations = jdbcOperations;
        this.rowMapper = new EntityRowMapper<>(
            (RelationalPersistentEntity<Issue>) mappingContext.getRequiredPersistentEntity(Issue.class),
            jdbcConverter);
    }

    @Override
    public Page<Issue> findByRepoIdAndAttachedLabelsLabelId(
        AggregateReference<Repo, String> repoId,
        AggregateReference<Label, UUID> labelId,
        Pageable pageable) {

        // SORT property 는 entity property 명으로 변경되야 한다.
        // (https://github.com/spring-projects/spring-data-jdbc/pull/210)
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("repoId", repoId.getId())
            .addValue("labelId", labelId.getId())
            .addValue("offset", pageable.getOffset())
            .addValue("pageSize", pageable.getPageSize());

        List<Issue> issues = this.jdbcOperations.query(
            IssueSql.selectByRepoIdAndAttachedLabelsLabelId(pageable.getSort()), parameterSource, this.rowMapper);

        return PageableExecutionUtils.getPage(issues, pageable, () ->
            this.jdbcOperations.queryForObject(IssueSql.countByRepoIdAndAttachedLabelsLabelId(), parameterSource, Long.class));
    }
}
