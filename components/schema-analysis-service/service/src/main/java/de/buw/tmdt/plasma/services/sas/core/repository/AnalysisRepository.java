package de.buw.tmdt.plasma.services.sas.core.repository;

import de.buw.tmdt.plasma.services.sas.core.model.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.UUID;

@Transactional
@Repository
public interface AnalysisRepository extends JpaRepository<Analysis, UUID> {

}
