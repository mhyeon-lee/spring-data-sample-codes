package spring.data.r2dbc.support;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import spring.data.r2dbc.label.Label;

@Aspect
public class PersistableMarkNotNewAop {
    @Around("this(org.springframework.data.repository.reactive.ReactiveCrudRepository)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object publisher = joinPoint.proceed();

        String methodName = joinPoint.getSignature().getName();
        if (methodName.startsWith("save") || methodName.equals("insert")) { // "insert" for WithInsert
            if (publisher instanceof Mono) {
                return ((Mono<?>) publisher).doOnNext(it -> {
                    if (it instanceof Label) {
                        ((Label) it).markNotNew();
                    }
                });
            }
            if (publisher instanceof Flux) {
                return ((Flux<?>) publisher).doOnNext(it -> {
                    if (it instanceof Label) {
                        ((Label) it).markNotNew();
                    }
                });
            }
        }

        return publisher;
    }
}
