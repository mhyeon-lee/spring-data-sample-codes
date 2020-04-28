package com.navercorp.spring.sql.groovy.issue.sql

import com.navercorp.spring.data.jdbc.plus.sql.support.SqlGeneratorSupport
import com.navercorp.spring.sql.groovy.issue.Issue
import org.springframework.data.domain.Sort

import static java.util.stream.Collectors.joining

class IssueSql extends SqlGeneratorSupport {

    String selectByRepoIdAndAttachedLabelsLabelId(Sort sort) {
        """
        SELECT ${sql.aggregateColumns(Issue)} 
        FROM ${sql.aggregateTables(Issue)}
        WHERE
        REPO_ID = :repoId
        AND attachedLabels.LABEL_ID = :labelId
        ORDER BY ${orderBy(sort)}
        LIMIT :pageSize OFFSET :offset
        """
    }

    String countByRepoIdAndAttachedLabelsLabelId() {
        """
        SELECT COUNT(*)
        FROM ${sql.aggregateTables(Issue)}
        WHERE
        REPO_ID = :repoId
        AND attachedLabels.LABEL_ID = :labelId
        """
    }

    // TODO: SORT property ORDER BY 는 COLUMN 으로 변환해야 한다.
    // (https://github.com/spring-projects/spring-data-jdbc/pull/210)
    static CharSequence orderBy(Sort sort) {
        return sort.stream()
                .map(order -> order.getProperty() + " " + order.getDirection().name())
                .collect(joining(", "))
    }
}
