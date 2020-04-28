package spring.data.r2dbc.test;

import org.springframework.context.ApplicationContext;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.relational.core.query.CriteriaDefinition;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import spring.data.r2dbc.account.Account;
import spring.data.r2dbc.comment.Comment;
import spring.data.r2dbc.issue.Issue;
import spring.data.r2dbc.issue.IssueAttachedLabel;
import spring.data.r2dbc.label.Label;
import spring.data.r2dbc.repo.Repo;

public class DataInitializeExecutionListener extends AbstractTestExecutionListener {
    @Override
    public void afterTestMethod(TestContext testContext) {
        ApplicationContext applicationContext = testContext.getApplicationContext();
        DatabaseClient dbClient = applicationContext.getBean(DatabaseClient.class);
        dbClient.delete().from(Account.class).matching(CriteriaDefinition.empty()).fetch()
            .rowsUpdated()
            .then(dbClient.delete().from(IssueAttachedLabel.class).matching(CriteriaDefinition.empty()).fetch()
                .rowsUpdated())
            .then(dbClient.delete().from(Issue.class).matching(CriteriaDefinition.empty()).fetch()
                .rowsUpdated())
            .then(dbClient.delete().from(Label.class).matching(CriteriaDefinition.empty()).fetch()
                .rowsUpdated())
            .then(dbClient.delete().from(Repo.class).matching(CriteriaDefinition.empty()).fetch()
                .rowsUpdated())
            .then(dbClient.delete().from(Comment.class).matching(CriteriaDefinition.empty()).fetch()
                .rowsUpdated())
            .then()
            .block();
    }
}
