package spring.data.r2dbc.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public abstract class JsonStringWritingConverter<T> implements Converter<T, String> {
    private final Class<T> type;
    private final ObjectMapper objectMapper;

    public JsonStringWritingConverter(Class<T> type, ObjectMapper objectMapper) {
        this.type = type;
        this.objectMapper = objectMapper;
    }

    @Override
    public String convert(T source) {
        try {
            return this.objectMapper.writeValueAsString(source);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Serialize issueContent is failed.", ex);
        }
    }
}
