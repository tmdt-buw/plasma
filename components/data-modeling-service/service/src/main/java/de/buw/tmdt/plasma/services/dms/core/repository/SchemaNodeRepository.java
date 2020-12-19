package de.buw.tmdt.plasma.services.dms.core.repository;

import de.buw.tmdt.plasma.services.dms.core.model.datasource.syntaxmodel.Node;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Transactional
@Repository
public interface SchemaNodeRepository extends JpaRepository<Node, UUID> {

}