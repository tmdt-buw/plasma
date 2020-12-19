package de.buw.tmdt.plasma.services.dms.core.repository;

import de.buw.tmdt.plasma.services.dms.core.model.datasource.semanticmodel.RelationConcept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public interface RelationConceptRepository extends JpaRepository<RelationConcept, Long> {
    RelationConcept findByUuid(String uuid);    //todo: is string correct here?
}
