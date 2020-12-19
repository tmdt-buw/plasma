package de.buw.tmdt.plasma.services.dss.core.repository;


import de.buw.tmdt.plasma.services.dss.core.model.DataSourceModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.UUID;

@Transactional
@Repository
public interface DataSourceRepository extends JpaRepository<DataSourceModel, UUID> {

}