package de.buw.tmdt.plasma.datamodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.buw.tmdt.plasma.datamodel.modification.DeltaModification;
import de.buw.tmdt.plasma.datamodel.semanticmodel.ObjectProperty;
import de.buw.tmdt.plasma.datamodel.semanticmodel.Relation;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.Edge;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.UUID;

import static de.buw.tmdt.plasma.datamodel.CombinedModelGenerator.getCombinedModel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CombinedModelTest {


    @Test
    void testSerialize() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        CombinedModel model = getCombinedModel();

        String serialization = mapper.writeValueAsString(model);

        CombinedModel deserializedModel = mapper.readValue(serialization, CombinedModel.class);

        assertEquals(model.getSyntaxModel().getEdges(), deserializedModel.getSyntaxModel().getEdges());
        assertEquals(model.getSyntaxModel().getNodes(), deserializedModel.getSyntaxModel().getNodes());
        assertEquals(model.getSemanticModel().getEdges(), deserializedModel.getSemanticModel().getEdges());
        assertEquals(model.getSemanticModel().getEdges(), deserializedModel.getSemanticModel().getEdges());
        assertEquals(model.getRecommendations(), deserializedModel.getRecommendations());
        assertEquals(model.getProvisionalElements(), deserializedModel.getProvisionalElements());
        // assertEquals(model.getProvisionalRelationConcepts(), deserializedModel.getProvisionalRelationConcepts());
    }

    @Test
    void testCopy() {

        CombinedModel model = getCombinedModel();
        CombinedModel cloned = model.copy();

        assertEquals(model.getSyntaxModel().getRoot(), cloned.getSyntaxModel().getRoot());
        assertEquals(model.getSyntaxModel().getNodes().size(), cloned.getSyntaxModel().getNodes().size());
        assertEquals(model.getSyntaxModel().getEdges().size(), cloned.getSyntaxModel().getEdges().size());

        assertEquals(model.getSemanticModel().getId(), cloned.getSemanticModel().getId());
        assertEquals(model.getSemanticModel().getNodes().size(), cloned.getSemanticModel().getNodes().size());
        assertEquals(model.getSemanticModel().getEdges().size(), cloned.getSemanticModel().getEdges().size());

        assertEquals(model.getRecommendations().size(), cloned.getRecommendations().size());

        assertEquals(model.generateModelMappings().size(), cloned.generateModelMappings().size());

        assertEquals(model.getProvisionalElements().size(), cloned.getProvisionalElements().size());
        //assertEquals(model.getProvisionalRelationConcepts().size(), cloned.getProvisionalRelationConcepts().size());

        // TODO deep testing
    }

    @Test
    void testValidate() {
        CombinedModel model = getCombinedModel();
        Random rnd = new Random();

        // assure that generated models are valid
        model.validate();

        // test edge from not matching
        model = getCombinedModel();
        Edge edge = model.getSyntaxModel().getEdges().get(rnd.nextInt(model.getSyntaxModel().getEdges().size()));
        edge.setFromId(UUID.randomUUID().toString());

        assertThrows(CombinedModelIntegrityException.class, model::validate);

        // test edge to not matching
        model = getCombinedModel();
        edge = model.getSyntaxModel().getEdges().get(rnd.nextInt(model.getSyntaxModel().getEdges().size()));
        edge.setToId(UUID.randomUUID().toString());

        assertThrows(CombinedModelIntegrityException.class, model::validate);

        // test relation to not matching
        model = getCombinedModel();
        Relation relation = model.getSemanticModel().getEdges().get(rnd.nextInt(model.getSemanticModel().getEdges().size()));
        relation.setTo(UUID.randomUUID().toString());

        assertThrows(CombinedModelIntegrityException.class, model::validate);

        // test relation from not matching
        model = getCombinedModel();
        relation = model.getSemanticModel().getEdges().get(rnd.nextInt(model.getSemanticModel().getEdges().size()));
        relation.setFrom(UUID.randomUUID().toString());

        assertThrows(CombinedModelIntegrityException.class, model::validate);

        // test relation from not accepting syntax node ids
        model = getCombinedModel();
        relation = model.getSemanticModel().getEdges().get(rnd.nextInt(model.getSemanticModel().getEdges().size()));
        relation.setFrom(model.getSyntaxModel().getNodes().get(0).getUuid());

        assertThrows(CombinedModelIntegrityException.class, model::validate);

        // test rogue relation in recommendation
        model = getCombinedModel();
        DeltaModification mod = new DeltaModification("test", null, null, null, null);
        Relation r = new ObjectProperty(model.getSyntaxModel().getNodes().get(0).getUuid(),
                model.getSemanticModel().getNodes().get(0).getUuid(),
                model.getSemanticModel().getEdges().get(0).getURI());
        mod.getRelations().add(r);
        model.getRecommendations().add(mod);

        assertThrows(CombinedModelIntegrityException.class, model::validate);

        // test rogue relation in recommendation
        model = getCombinedModel();
        mod = new DeltaModification("test", null, null, null, null);
        r = new ObjectProperty(model.getSemanticModel().getNodes().get(0).getUuid(),
                UUID.randomUUID().toString(),
                model.getSemanticModel().getEdges().get(0).getURI());
        mod.getRelations().add(r);
        model.getRecommendations().add(mod);

        assertThrows(CombinedModelIntegrityException.class, model::validate);

    }

}