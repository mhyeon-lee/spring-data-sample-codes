package spring.data.r2dbc.support;

import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

public interface WithInsert<T> {
    R2dbcEntityOperations getR2dbcEntityOperations();

    @Transactional
    default Mono<T> insert(T t) {
        return this.getR2dbcEntityOperations().insert(t);
    }
}
