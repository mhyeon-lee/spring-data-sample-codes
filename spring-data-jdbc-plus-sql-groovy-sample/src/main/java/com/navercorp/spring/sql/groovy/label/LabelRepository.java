package com.navercorp.spring.sql.groovy.label;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface LabelRepository extends CrudRepository<Label, UUID> {
}
