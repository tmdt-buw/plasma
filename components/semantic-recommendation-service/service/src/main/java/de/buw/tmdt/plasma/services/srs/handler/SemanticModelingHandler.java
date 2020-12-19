package de.buw.tmdt.plasma.services.srs.handler;

import de.buw.tmdt.plasma.services.dms.shared.dto.DataSourceSchemaDTO;
import org.springframework.stereotype.Service;

@Service
public class SemanticModelingHandler {

    public DataSourceSchemaDTO performModeling(String uuid, DataSourceSchemaDTO dataSourceSchemaDTO) {
        // call ARS-R here
        return dataSourceSchemaDTO;
    }
}
