package de.buw.tmdt.plasma.services.srs.handler;

import de.buw.tmdt.plasma.ars.labeling.lm.shared.feignclient.LabelMatchingApiClient;
import de.buw.tmdt.plasma.services.dms.shared.dto.DataSourceSchemaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SemanticLabelingHandler {

    private static final Logger log = LoggerFactory.getLogger(SemanticLabelingHandler.class);

    final LabelMatchingApiClient labelMatchingApiClient;

    public SemanticLabelingHandler(LabelMatchingApiClient labelMatchingApiClient) {
        this.labelMatchingApiClient = labelMatchingApiClient;
    }

    public DataSourceSchemaDTO performLabeling(String uuid, DataSourceSchemaDTO dataSourceSchemaDTO) {
        try {
            // call ARS-L here
            dataSourceSchemaDTO = labelMatchingApiClient.performLabeling(uuid, dataSourceSchemaDTO);
        } catch (Exception ex) {
            log.warn("Could not perform semantic labeling", ex);
        }

        return dataSourceSchemaDTO;
    }
}
