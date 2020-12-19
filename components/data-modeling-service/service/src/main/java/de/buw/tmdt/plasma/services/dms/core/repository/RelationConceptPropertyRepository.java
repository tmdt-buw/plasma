package de.buw.tmdt.plasma.services.dms.core.repository;

import de.buw.tmdt.plasma.services.dms.core.model.datasource.semanticmodel.RelationConcept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface RelationConceptPropertyRepository extends JpaRepository<RelationConcept.Property, Long> {
	List<RelationConcept.Property> findAllByNameIn(Iterable<String> names);
}