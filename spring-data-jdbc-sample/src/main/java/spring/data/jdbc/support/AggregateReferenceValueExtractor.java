package spring.data.jdbc.support;

import org.springframework.data.jdbc.core.mapping.AggregateReference;

import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.ValueExtractor;

public class AggregateReferenceValueExtractor implements ValueExtractor<AggregateReference<?, @ExtractedValue ?>> {
    public AggregateReferenceValueExtractor() {
    }

    @Override
    public void extractValues(AggregateReference<?, ?> originalValue, ValueReceiver receiver) {
        receiver.value("id", originalValue.getId());
    }
}
