package de.buw.tmdt.plasma.services.dms.core.repository;

import de.buw.tmdt.plasma.services.dms.core.model.datasource.semanticmodel.EntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public interface EntityTypeRepository extends JpaRepository<EntityType, Long> {
}
