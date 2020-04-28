package com.navercorp.spring.sql.groovy.query.sql

import com.navercorp.spring.data.jdbc.plus.sql.support.SqlGeneratorSupport
import com.navercorp.spring.sql.groovy.query.criteria.IssueGridCriteria
import com.navercorp.spring.sql.groovy.query.criteria.IssueViewCriteria
import com.navercorp.spring.sql.groovy.query.grid.IssueGrid
import com.navercorp.spring.sql.groovy.query.view.IssueView
import org.springframework.data.domain.Sort

import java.util.function.Supplier

import static java.util.stream.Collectors.joining

class QuerySideSql extends SqlGeneratorSupport {

    String countIssueGrids(IssueGridCriteria criteria) {
        """
        SELECT COUNT(*)
        ${fromIssueGrid()}
        ${whereIssueGrids(criteria)}
        """
    }

    String selectIssueGrids(IssueGridCriteria criteria, Sort sort) {
        """
        SELECT ${sql.columns(IssueGrid)} 
        ${fromIssueGrid()}
        ${whereIssueGrids(criteria)}
        ORDER BY ${orderBy(sort)}
        LIMIT :pageSize OFFSET :offset
        """
    }

    private static String fromIssueGrid() {
        """
        FROM ISSUE
        INNER JOIN ISSUE_CONTENT content
        ON ISSUE.ID = content.ISSUE_ID
        
        INNER JOIN ACCOUNT creator
        ON ISSUE.CREATED_BY = creator.ID
        
        INNER JOIN REPO repo
        ON ISSUE.REPO_ID = repo.ID
        
        LEFT OUTER JOIN COMMENT comment
        ON ISSUE.ID = comment.ISSUE_ID
        LEFT OUTER JOIN COMMENT_CONTENT comment_content
        ON comment.ID = comment_content.ID
        LEFT OUTER JOIN ACCOUNT comment_creator
        ON comment.CREATED_BY = comment_creator.ID
        """
    }

    private static String whereIssueGrids(IssueGridCriteria criteria) {
        """
        WHERE 1 = 1
        ${
            ifNotNull(criteria.status) {
                "AND ISSUE.STATUS = :status"
            }
        }
        ${
            ifNotNull(criteria.createdBy) {
                "AND ISSUE.CREATED_BY = :createdBy"
            }
        }
        ${
            ifNotNull(criteria.searchRepoName) {
                "AND repo.NAME LIKE :searchRepoName"
            }
        }
        ${
            ifNotNull(criteria.searchContent) {
                "AND CONCAT(ISSUE.TITLE, content.BODY, comment_content.BODY) LIKE :searchContent"
            }
        }
        """
    }

    String selectIssueView() {
        """
        SELECT ${sql.aggregateColumns(IssueView)} 
        ${fromIssueView()}
        
        WHERE ISSUE.ID = :issueId
        ORDER BY issue_attached_labels.ATTACHED_AT ASC, comments.CREATED_AT ASC
        """
    }

    String countIssueViews(IssueViewCriteria criteria) {
        """
        SELECT COUNT(DISTINCT ISSUE.ID) 
        ${fromIssueViewFilter(criteria)}
        ${whereIssueViews(criteria)}
        """
    }

    String selectIssueViewIds(IssueViewCriteria criteria, Sort sort) {
        """
        SELECT DISTINCT VIEW.ISSUE_ID
        FROM (
            SELECT ISSUE.ID AS ISSUE_ID, ISSUE.ISSUE_NO AS ISSUE_NO
            ${fromIssueViewFilter(criteria)}
            ${whereIssueViews(criteria)}
            ORDER BY ${orderBy(sort)}
            LIMIT :pageSize OFFSET :offset
        ) VIEW
        """
    }

    String selectIssueViews(Sort sort) {
        """
        SELECT ${sql.aggregateColumns(IssueView)} 
        ${fromIssueView()}
        WHERE ISSUE.ID IN (:issueIds)
        ORDER BY ${orderBy(sort)}
        """
    }

    private static String fromIssueView() {
        """
        FROM ISSUE
        INNER JOIN ISSUE_CONTENT content
        ON ISSUE.ID = content.ISSUE_ID
        
        INNER JOIN ACCOUNT creator
        ON ISSUE.CREATED_BY = creator.ID
        
        INNER JOIN REPO repo
        ON ISSUE.REPO_ID = repo.ID
        
        LEFT OUTER JOIN ISSUE_ATTACHED_LABEL issue_attached_labels
        ON ISSUE.ID = issue_attached_labels.ISSUE_ID
        LEFT OUTER JOIN LABEL labels
        ON issue_attached_labels.LABEL_ID = labels.ID
        
        LEFT OUTER JOIN COMMENT comments
        ON ISSUE.ID = comments.ISSUE_ID
        LEFT OUTER JOIN COMMENT_CONTENT comments_content
        ON comments.ID = comments_content.ID
        LEFT OUTER JOIN ACCOUNT comments_creator
        ON comments.CREATED_BY = comments_creator.ID
        """
    }

    // Filter 에 포함되는 테이블만 JOIN 한다.
    private static String fromIssueViewFilter(IssueViewCriteria criteria) {
        """
        FROM ISSUE
        ${
            ifNotNull(criteria.searchContent) {
                """
                INNER JOIN ISSUE_CONTENT content
                ON ISSUE.ID = content.ISSUE_ID
                
                LEFT OUTER JOIN COMMENT comments
                ON ISSUE.ID = comments.ISSUE_ID
                LEFT OUTER JOIN COMMENT_CONTENT comments_content
                ON comments.ID = comments_content.ID
                """
            }
        }
        ${
            ifNotNull(criteria.searchRepoName) {
                """
                INNER JOIN REPO repo
                ON ISSUE.REPO_ID = repo.ID
                """
            }
        }
        ${
            ifNotEmpty(criteria.labelIds) {
                """
                LEFT OUTER JOIN ISSUE_ATTACHED_LABEL issue_attached_labels
                ON ISSUE.ID = issue_attached_labels.ISSUE_ID
                """
            }
        }
        """
    }

    private static String whereIssueViews(IssueViewCriteria criteria) {
        """
        WHERE 1 = 1
        ${
            ifNotNull(criteria.status) {
                "AND ISSUE.STATUS = :status"
            }
        }
        ${
            ifNotNull(criteria.createdBy) {
                "AND ISSUE.CREATED_BY = :createdBy"
            }
        }
        ${
            ifNotEmpty(criteria.labelIds) {
                "AND issue_attached_labels.LABEL_ID IN (:labelIds)"
            }
        }
        ${
            ifNotNull(criteria.searchRepoName) {
                "AND repo.NAME LIKE :searchRepoName"
            }
        }
        ${
            ifNotNull(criteria.searchContent) {
                "AND CONCAT(ISSUE.TITLE, content.BODY, comments_content.BODY) LIKE :searchContent"
            }
        }
        """
    }

    static CharSequence ifNotNull(Object param, Supplier<CharSequence> sqlSupplier) {
        if (param == null) {
            return ""
        }
        CharSequence sql = sqlSupplier.get();
        if (sql == null) {
            return ""
        }
        return sql
    }

    static CharSequence ifNotEmpty(List<?> param, Supplier<CharSequence> sqlSupplier) {
        if (param == null || param.isEmpty()) {
            return ""
        }
        CharSequence sql = sqlSupplier.get();
        if (sql == null) {
            return ""
        }
        return sql
    }

    // TODO: SORT property ORDER BY 는 COLUMN 으로 변환해야 한다.
    // (https://github.com/spring-projects/spring-data-jdbc/pull/210)
    static CharSequence orderBy(Sort sort) {
        if (sort.isUnsorted()) {
            return "issue_attached_labels.ATTACHED_AT ASC, comments.CREATED_AT ASC"
        }
        return sort.stream()
                .map(order -> order.getProperty() + " " + order.getDirection().name())
                .collect(joining(", "))
    }
}
