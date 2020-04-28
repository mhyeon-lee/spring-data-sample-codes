package com.navercorp.spring.sql.groovy.query.criteria;

import com.navercorp.spring.sql.groovy.account.Account;
import com.navercorp.spring.sql.groovy.issue.Status;
import lombok.Builder;
import lombok.Value;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

import java.util.UUID;

@Value
@Builder
public class IssueGridCriteria {
    Status status;

    AggregateReference<Account, UUID> createdBy;

    String searchRepoName;

    String searchContent;
}
