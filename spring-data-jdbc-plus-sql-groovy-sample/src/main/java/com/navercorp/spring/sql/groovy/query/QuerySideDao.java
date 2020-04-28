package com.navercorp.spring.sql.groovy.query;

import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider;
import com.navercorp.spring.data.jdbc.plus.sql.support.JdbcDaoSupport;
import com.navercorp.spring.data.jdbc.plus.sql.support.trait.SingleValueSelectTrait;
import com.navercorp.spring.sql.groovy.query.criteria.IssueGridCriteria;
import com.navercorp.spring.sql.groovy.query.criteria.IssueViewCriteria;
import com.navercorp.spring.sql.groovy.query.grid.IssueGrid;
import com.navercorp.spring.sql.groovy.query.sql.QuerySideSql;
import com.navercorp.spring.sql.groovy.query.view.IssueView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class QuerySideDao extends JdbcDaoSupport implements SingleValueSelectTrait {
    private final QuerySideSql sqls;

    protected QuerySideDao(EntityJdbcProvider entityJdbcProvider) {
        super(entityJdbcProvider);
        this.sqls = super.sqls(QuerySideSql::new);
    }

    public Page<IssueGrid> selectIssueGrid(IssueGridCriteria criteria, Pageable pageable) {
        List<IssueGrid> content = this.select(
            this.sqls.selectIssueGrids(criteria, pageable.getSort()),
            compositeSqlParameterSource(
                beanParameterSource(criteria),
                mapParameterSource()
                    .addValue("pageSize", pageable.getPageSize())
                    .addValue("offset", pageable.getOffset())
            ),
            IssueGrid.class
        );

        return PageableExecutionUtils.getPage(content, pageable, () ->
            this.selectSingleValue(this.sqls.countIssueGrids(criteria), beanParameterSource(criteria), Long.class));
    }

    public IssueView selectIssueView(UUID issueId) {
        return this.requiredOne(
            this.sqls.selectIssueView(),
            mapParameterSource().addValue("issueId", issueId),
            this.getAggregateResultSetExtractor(IssueView.class));
    }

    public Page<IssueView> selectIssueViews(IssueViewCriteria criteria, Pageable pageable) {
        // 1. 결과 대상 filter
        List<UUID> issueIds = this.select(
            this.sqls.selectIssueViewIds(criteria, pageable.getSort()),
            compositeSqlParameterSource(
                beanParameterSource(criteria),
                mapParameterSource()
                    .addValue("pageSize", pageable.getPageSize())
                    .addValue("offset", pageable.getOffset())
            ), new SingleColumnRowMapper<>(UUID.class)
        );

        // 2. filter 된 ID 기반으로 Aggregate 조회
        List<IssueView> content = this.select(
            this.sqls.selectIssueViews(pageable.getSort()),
            mapParameterSource()
                .addValue("issueIds", issueIds),
            this.getAggregateResultSetExtractor(IssueView.class)
        );

        // 3. 결과 + COUNT  ->  Paging
        return PageableExecutionUtils.getPage(content, pageable, () ->
            this.selectSingleValue(this.sqls.countIssueViews(criteria), beanParameterSource(criteria), Long.class));
    }
}
