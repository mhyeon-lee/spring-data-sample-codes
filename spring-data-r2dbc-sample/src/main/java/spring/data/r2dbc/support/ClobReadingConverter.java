package spring.data.r2dbc.support;

import io.r2dbc.spi.Clob;
import org.springframework.core.convert.converter.Converter;
import reactor.core.publisher.Mono;

public interface ClobReadingConverter<T> extends Converter<Clob, T> {
    default Mono<String> convertToString(Clob source) {
        return Mono.from(source.stream())
            .map(CharSequence::toString);
    }
}
