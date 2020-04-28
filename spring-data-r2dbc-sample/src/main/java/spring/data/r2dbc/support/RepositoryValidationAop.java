package spring.data.r2dbc.support;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.Set;

@Aspect
public class RepositoryValidationAop {
    private final Validator validator;

    public RepositoryValidationAop(Validator validator) {
        this.validator = validator;
    }

    @Around("this(org.springframework.data.repository.reactive.ReactiveCrudRepository)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object publisher = joinPoint.proceed();

        String methodName = joinPoint.getSignature().getName();
        if (methodName.startsWith("save") || methodName.equals("insert")) { // "insert" for WithInsert
            if (publisher instanceof Mono) {
                return this.validate(joinPoint.getArgs()[0])
                    .then((Mono<?>) publisher);
            }
            if (publisher instanceof Flux) {
                return this.validate(joinPoint.getArgs()[0])
                    .thenMany((Flux<?>) publisher);
            }
        }

        return publisher;
    }

    @SuppressWarnings("rawtypes")
    private Flux<?> validate(Object arg) {
        Flux<?> flux = Flux.empty();
        if (arg instanceof Iterable) {
            flux = flux.concatWith(Flux.fromIterable((Iterable) arg));
        } else if (arg instanceof Publisher) {
            flux = flux.concatWith((Publisher) arg);
        } else {
            flux = Flux.just(arg);
        }

        return flux.doOnNext(it -> {
            Set<ConstraintViolation<Object>> violations = this.validator.validate(it);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
        });
    }
}
