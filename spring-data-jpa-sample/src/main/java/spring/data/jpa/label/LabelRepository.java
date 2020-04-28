package spring.data.jpa.label;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LabelRepository extends JpaRepository<Label, UUID> {
}
