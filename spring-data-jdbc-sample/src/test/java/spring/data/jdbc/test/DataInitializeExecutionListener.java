package spring.data.jdbc.test;

import org.springframework.context.ApplicationContext;
import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import spring.data.jdbc.account.Account;
import spring.data.jdbc.comment.Comment;
import spring.data.jdbc.issue.Issue;
import spring.data.jdbc.label.Label;
import spring.data.jdbc.repo.Repo;

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
