package spring.data.r2dbc.support;

import org.springframework.data.r2dbc.core.R2dbcEntityOperations;

public class WithInsertImpl<T> implements WithInsert<T> {
    private final R2dbcEntityOperations r2dbcEntityOperations;

    public WithInsertImpl(R2dbcEntityOperations r2dbcEntityOperations) {
        this.r2dbcEntityOperations = r2dbcEntityOperations;
    }

    @Override
    public R2dbcEntityOperations getR2dbcEntityOperations() {
        return this.r2dbcEntityOperations;
    }
}
