package spring.data.jdbc.support;

import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.transaction.annotation.Transactional;

public interface WithInsert<T> {
    JdbcAggregateOperations getJdbcAggregateOperations();

    @Transactional
    default T insert(T t) {
        return this.getJdbcAggregateOperations().insert(t);
    }
}
