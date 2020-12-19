package de.buw.tmdt.plasma.services.sas.core.repository;

import de.buw.tmdt.plasma.services.sas.core.model.semanticmodel.RelationConcept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public interface RelationConceptPropertyRepository extends JpaRepository<RelationConcept.Property, Long> {
	List<RelationConcept.Property> findAllByNameIn(Iterable<String> names);
}