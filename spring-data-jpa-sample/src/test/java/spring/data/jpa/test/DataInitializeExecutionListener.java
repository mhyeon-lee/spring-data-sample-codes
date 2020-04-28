package spring.data.jpa.test;

import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;

public class DataInitializeExecutionListener extends AbstractTestExecutionListener {
    @Override
    public void afterTestMethod(TestContext testContext) {
        ApplicationContext applicationContext = testContext.getApplicationContext();
        TransactionTemplate transactionTemplate = applicationContext.getBean(TransactionTemplate.class);
        transactionTemplate.execute(status -> {
            EntityManager em = applicationContext.getBean(EntityManager.class);
            em.createQuery("SELECT i FROM Issue i").getResultStream().forEach(em::remove);
            em.createQuery("SELECT l FROM Label l").getResultStream().forEach(em::remove);
            em.createQuery("SELECT r FROM Repo r").getResultStream().forEach(em::remove);
            em.createQuery("SELECT c FROM Comment c").getResultStream().forEach(em::remove);

            // REMOVE 는 UPDATE(SOFT DELETE) 가 되므로 직접 삭제 쿼리를 작성한다.
            // 연관관계가 추가된다면, 연관관계들도 같이 삭제 추가해야 한다.
            em.createQuery("DELETE FROM Account").executeUpdate();
            em.flush();
            return null;
        });
    }
}
