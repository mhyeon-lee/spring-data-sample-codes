package spring.data.r2dbc.label;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import java.util.UUID;

public interface LabelRepository extends R2dbcRepository<Label, UUID> {
}
