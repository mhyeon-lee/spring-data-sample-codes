package com.navercorp.spring.sql.groovy.issue;

import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider;
import com.navercorp.spring.data.jdbc.plus.sql.support.JdbcRepositorySupport;
import com.navercorp.spring.data.jdbc.plus.sql.support.trait.SingleValueSelectTrait;
import com.navercorp.spring.sql.groovy.issue.sql.IssueSql;
import com.navercorp.spring.sql.groovy.label.Label;
import com.navercorp.spring.sql.groovy.repo.Repo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.List;
import java.util.UUID;

public class IssueRepositoryImpl extends JdbcRepositorySupport<Issue>
    implements IssueRepositoryCustom, SingleValueSelectTrait {

    private final IssueSql sqls;

    @SuppressWarnings("unchecked")
    public IssueRepositoryImpl(EntityJdbcProvider entityJdbcProvider) {

        super(Issue.class, entityJdbcProvider);
        this.sqls = sqls(IssueSql::new);
    }

    @Override
    public Page<Issue> findByRepoIdAndAttachedLabelsLabelId(
        AggregateReference<Repo, String> repoId,
        AggregateReference<Label, UUID> labelId,
        Pageable pageable) {

        SqlParameterSource parameterSource = mapParameterSource()
            .addValue("repoId", repoId.getId())
            .addValue("labelId", labelId.getId())
            .addValue("offset", pageable.getOffset())
            .addValue("pageSize", pageable.getPageSize());

        List<Issue> content = find(this.sqls.selectByRepoIdAndAttachedLabelsLabelId(pageable.getSort()), parameterSource);
        return PageableExecutionUtils.getPage(content, pageable,
            () -> selectSingleValue(this.sqls.countByRepoIdAndAttachedLabelsLabelId(), parameterSource, Long.class));
    }
}
