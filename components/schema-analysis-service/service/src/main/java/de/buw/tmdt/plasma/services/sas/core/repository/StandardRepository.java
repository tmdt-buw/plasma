package de.buw.tmdt.plasma.services.sas.core.repository;

import de.buw.tmdt.plasma.services.sas.core.model.Standard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StandardRepository extends JpaRepository<Standard, UUID> {

	List<Standard> findAllByName(String name);

}
