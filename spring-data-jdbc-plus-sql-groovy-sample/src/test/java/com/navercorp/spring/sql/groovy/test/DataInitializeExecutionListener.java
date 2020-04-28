package com.navercorp.spring.sql.groovy.test;

import com.navercorp.spring.sql.groovy.account.Account;
import com.navercorp.spring.sql.groovy.comment.Comment;
import com.navercorp.spring.sql.groovy.issue.Issue;
import com.navercorp.spring.sql.groovy.label.Label;
import com.navercorp.spring.sql.groovy.repo.Repo;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class DataInitializeExecutionListener extends AbstractTestExecutionListener {
    @Override
    public void afterTestMethod(TestContext testContext) {
        ApplicationContext applicationContext = testContext.getApplicationContext();
        JdbcAggregateOperations jdbcAggregateOperations = applicationContext.getBean(JdbcAggregateOperations.class);
        jdbcAggregateOperations.deleteAll(Account.class);
        jdbcAggregateOperations.deleteAll(Issue.class);
        jdbcAggregateOperations.deleteAll(Label.class);
        jdbcAggregateOperations.deleteAll(Repo.class);
        jdbcAggregateOperations.deleteAll(Comment.class);
    }
}
