package spring.data.r2dbc.issue.sql;

import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.query.QueryMapper;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import spring.data.r2dbc.issue.Issue;

import static java.util.stream.Collectors.joining;

public class IssueSql {
    private final RelationalPersistentEntity<Issue> entity;
    private final QueryMapper queryMapper;

    public IssueSql(RelationalPersistentEntity<Issue> entity, QueryMapper queryMapper) {
        this.entity = entity;
        this.queryMapper = queryMapper;
    }

    public String selectByRepoIdAndAttachedLabelsLabelId(Sort sort) {
        return new StringBuilder()
            .append("SELECT ISSUE.ID AS ID, ISSUE.VERSION AS VERSION, ISSUE.REPO_ID AS REPO_ID")
            .append(", ISSUE.ISSUE_NO AS ISSUE_NO, ISSUE.STATUS AS STATUS, ISSUE.TITLE AS TITLE")
            .append(", ISSUE.CREATED_BY AS CREATED_BY, ISSUE.CREATED_AT AS CREATED_AT")
            .append(", ISSUE.CONTENT AS CONTENT")
            .append(" FROM ISSUE")
            .append(" LEFT OUTER JOIN ISSUE_ATTACHED_LABEL ATTACHED_LABELS")
            .append(" ON ISSUE.ID = ATTACHED_LABELS.ISSUE_ID")
            .append(" WHERE")
            .append(" REPO_ID = :repoId")
            .append(" AND ATTACHED_LABELS.LABEL_ID = :labelId")
            .append(" ORDER BY ").append(orderBy(sort))
            .append(" LIMIT :pageSize OFFSET :offset")
            .toString();
    }

    private String orderBy(Sort sort) {
        return this.queryMapper.getMappedObject(sort, this.entity).stream()
            .map(order -> order.getProperty() + " " + order.getDirection().name())
            .collect(joining(", "));
    }
}
