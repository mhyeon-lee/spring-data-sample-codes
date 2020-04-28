package spring.data.jdbc.issue.sql;

import org.springframework.data.domain.Sort;

import static java.util.stream.Collectors.joining;

public class IssueSql {
    public static String selectByRepoIdAndAttachedLabelsLabelId(Sort sort) {
        return new StringBuilder()
            .append("SELECT ISSUE.ID AS ID, ISSUE.VERSION AS VERSION, ISSUE.REPO_ID AS REPO_ID")
            .append(", ISSUE.ISSUE_NO AS ISSUE_NO, ISSUE.STATUS AS STATUS, ISSUE.TITLE AS TITLE")
            .append(", ISSUE.CREATED_BY AS CREATED_BY, ISSUE.CREATED_AT AS CREATED_AT")
            .append(", CONTENT.BODY AS CONTENT_BODY, CONTENT.MIME_TYPE AS CONTENT_MIME_TYPE")
            .append(" FROM ISSUE")
            .append(" LEFT OUTER JOIN ISSUE_CONTENT CONTENT")
            .append(" ON ISSUE.ID = CONTENT.ISSUE_ID")
            .append(" LEFT OUTER JOIN ISSUE_ATTACHED_LABEL ATTACHED_LABELS")
            .append(" ON ISSUE.ID = ATTACHED_LABELS.ISSUE_ID")
            .append(" WHERE")
            .append(" REPO_ID = :repoId")
            .append(" AND ATTACHED_LABELS.LABEL_ID = :labelId")
            .append(" ORDER BY ").append(orderBy(sort))
            .append(" LIMIT :pageSize OFFSET :offset")
            .toString();
    }

    public static String countByRepoIdAndAttachedLabelsLabelId() {
        return  new StringBuilder()
            .append("SELECT COUNT(*)")
            .append(" FROM ISSUE")
            .append(" LEFT OUTER JOIN ISSUE_CONTENT CONTENT")
            .append(" ON ISSUE.ID = CONTENT.ISSUE_ID")
            .append(" LEFT OUTER JOIN ISSUE_ATTACHED_LABEL ATTACHED_LABELS")
            .append(" ON ISSUE.ID = ATTACHED_LABELS.ISSUE_ID")
            .append(" WHERE")
            .append(" REPO_ID = :repoId")
            .append(" AND ATTACHED_LABELS.LABEL_ID = :labelId")
            .toString();
    }

    private static String orderBy(Sort sort) {
        return sort.stream()
            .map(order -> order.getProperty() + " " + order.getDirection().name())
            .collect(joining(", "));
    }
}
