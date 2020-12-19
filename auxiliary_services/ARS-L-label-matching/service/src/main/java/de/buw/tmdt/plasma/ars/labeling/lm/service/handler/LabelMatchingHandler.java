package de.buw.tmdt.plasma.ars.labeling.lm.service.handler;

import de.buw.tmdt.plasma.ars.labeling.lm.service.converter.KGStoSRSDTOConverter;
import de.buw.tmdt.plasma.services.dms.shared.dto.DataSourceSchemaDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.PrimitiveEntityTypeEdgeDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.semanticmodel.EntityConceptDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.semanticmodel.EntityTypeDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.EdgeDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.PrimitiveDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.SchemaNodeDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.SyntaxModelDTO;
import de.buw.tmdt.plasma.services.kgs.shared.api.UniversalKnowledgeApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
public class LabelMatchingHandler {

    private static Logger log = LoggerFactory.getLogger(LabelMatchingHandler.class);

    final UniversalKnowledgeApi universalKnowledgeApi;

    public LabelMatchingHandler(UniversalKnowledgeApi universalKnowledgeApi) {
        this.universalKnowledgeApi = universalKnowledgeApi;
    }

    public DataSourceSchemaDTO performLabeling(String dataSourceUUID, DataSourceSchemaDTO combined) {
        SyntaxModelDTO syntaxModelDTO = combined.getSyntaxModelDTO();

        long counter = -1;
        for (SchemaNodeDTO node : syntaxModelDTO.getNodes()) {
            if(!(node instanceof PrimitiveDTO)) {
                continue;
            }

            String label = node.getLabel();
            Collection<EntityConceptDTO> entityConcepts = KGStoSRSDTOConverter.toDMS(universalKnowledgeApi.getEntityConcepts(label));
            Optional<EntityConceptDTO> matching = entityConcepts.stream().filter(entityConceptDTO -> entityConceptDTO.getName().equals(label)).findFirst();
            if(matching.isPresent()){  // we only match on exact equality
                // we found one
                // create an EntityType in the semantic model
                EntityTypeDTO et = new EntityTypeDTO(null,null,counter, label,label,"",matching.get());
                combined.getSemanticModelDTO().getNodes().add(et);
                // add a mapping from ET to node
                Optional<EdgeDTO> firstEdge = syntaxModelDTO.getEdges().stream().filter(edgeDTO -> edgeDTO.getTo().equals(node.getUuid())).findFirst();
                if(firstEdge.isPresent()){
                    // we got an edge find where it comes from
                    EdgeDTO edge = firstEdge.get();
                    Optional<SchemaNodeDTO> parentNode = syntaxModelDTO.getNodes().stream().filter(schemaNodeDTO -> schemaNodeDTO.getUuid().equals(edge.getFrom())).findFirst();
                    if(parentNode.isPresent()){
                        PrimitiveEntityTypeEdgeDTO mapping = new PrimitiveEntityTypeEdgeDTO(parentNode.get().getUuid().toString(),String.valueOf(counter),node.getUUIDString());
                        combined.getPrimitiveEntityTypeEdgeDTOs().add(mapping);
                        log.info("Assigned EC '{}' to node '{}'", matching.get().getName(), node.getLabel());
                        counter--;
                    }
                    else {
                        log.info("Could not assign EC '{}' to node '{}'. No parent found", matching.get().getName(), node.getLabel());
                    }
                } else {
                    log.info("Could not assign EC '{}' to node '{}'. No incoming edge found", matching.get().getName(), node.getLabel());
                }

            }
        }
        return combined;
    }
}
