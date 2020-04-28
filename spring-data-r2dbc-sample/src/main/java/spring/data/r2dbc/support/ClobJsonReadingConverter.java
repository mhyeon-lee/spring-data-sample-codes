package spring.data.r2dbc.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.spi.Clob;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class ClobJsonReadingConverter<T> implements ClobReadingConverter<T> {
    private final Class<T> type;
    private final ObjectMapper objectMapper;

    public ClobJsonReadingConverter(Class<T> type, ObjectMapper objectMapper) {
        this.type = type;
        this.objectMapper = objectMapper;
    }

    @Override
    public T convert(Clob source) {
        return convertToString(source)
            .map(content -> {
                try {
                    return this.objectMapper.readValue(content, this.type);
                } catch (JsonProcessingException ex) {
                    throw new RuntimeException("Deserialize issueContent is failed.", ex);
                }
            })
            .block();
    }
}
