package de.buw.tmdt.plasma.ars.labeling.lm.service.handler;

import de.buw.tmdt.plasma.datamodel.CombinedModel;
import de.buw.tmdt.plasma.datamodel.semanticmodel.Class;
import de.buw.tmdt.plasma.datamodel.semanticmodel.MappableSemanticModelNode;
import de.buw.tmdt.plasma.datamodel.semanticmodel.SemanticModelNode;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.PrimitiveNode;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.SchemaNode;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.SyntaxModel;
import de.buw.tmdt.plasma.services.kgs.shared.api.OntologyApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Service
public class LabelMatchingHandler {

    private static final Logger log = LoggerFactory.getLogger(LabelMatchingHandler.class);

    final OntologyApi ontologyApi;

    public LabelMatchingHandler(OntologyApi ontologyApi) {
        this.ontologyApi = ontologyApi;
    }

    public CombinedModel performLabeling(String id, String configId, String configToken, CombinedModel combined) {
        List<String> mappedPrimitiveNodeIds = combined.getSemanticModel().getNodes().stream()
                .filter(SemanticModelNode::isMapped)
                .map(n -> (MappableSemanticModelNode) n)
                .map(MappableSemanticModelNode::getMappedSyntaxNodeUuid)
                .collect(Collectors.toList());
        SyntaxModel syntaxModel = combined.getSyntaxModel();
        for (SchemaNode node : syntaxModel.getNodes()) {
            if (!(node instanceof PrimitiveNode) || mappedPrimitiveNodeIds.contains(node.getUuid())) {
                continue;
            }
            String label = node.getLabel();
            Collection<SemanticModelNode> candidates = ontologyApi.getElements(null,label, null, null);
            List<Class> classes = candidates.stream()
                    .filter(c -> c instanceof Class)
                    .map(c -> (Class) c)
                    .collect(Collectors.toList());


            Optional<Class> matching = classes.stream().filter(clazz -> clazz.getLabel().equalsIgnoreCase(label)).findFirst();
            if (matching.isPresent()) {  // we only match on exact equality but case-insensitive
                // we found one or more, create a mapping
                Class clazz = matching.get();
                clazz.createInstance();
                clazz.setMappedSyntaxNodeLabel(node.getLabel());
                clazz.setMappedSyntaxNodeUuid(node.getUuid());
                clazz.setMappedSyntaxNodePath(node.getPathAsJSONPointer());
                clazz.setXCoordinate(node.getXCoordinate());
                clazz.setYCoordinate(node.getYCoordinate());
                if (clazz.getInstance() != null) {
                    clazz.getInstance().setLabel(node.getLabel());
                }
                combined.getSemanticModel().getNodes().add(clazz);
            }
        }
        return combined;
    }
}
