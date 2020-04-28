package com.navercorp.spring.sql.groovy.issue;

import com.navercorp.spring.sql.groovy.label.Label;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class IssueAttachedLabel {
    @Id
    @With
    Long id;

    AggregateReference<Label, UUID> labelId;

    Instant attachedAt;
}
