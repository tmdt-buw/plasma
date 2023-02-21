package de.buw.tmdt.plasma.services.dms.core.model;

import de.buw.tmdt.plasma.datamodel.CombinedModel;
import de.buw.tmdt.plasma.datamodel.modification.operation.DataType;
import de.buw.tmdt.plasma.datamodel.semanticmodel.Class;
import de.buw.tmdt.plasma.datamodel.semanticmodel.SemanticModel;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test to simulate and control stack behaviour for {@link CombinedModel}s in the {@link Modeling}.
 */
class ModelingTest {

    Modeling modeling;

    int counter = 1;
    int modelcounter = 1;

    @BeforeEach
    void setUp() {
        modeling = new Modeling();
        modelcounter = 1;
        counter = 1;
    }

    @Test
    void getCurrentModel() {
        CombinedModel model = getCombinedModel();
        modeling.pushCombinedModel(model);
        assertEquals(model, modeling.getCurrentModel());
    }

    @Test
    void getCurrentModelOnEmptyStack() {
        assertThrows(UnsupportedOperationException.class, () -> modeling.getCurrentModel());
    }

    @Test
    void pushCombinedModel() {
        CombinedModel model;
        int count = 0;
        assertEquals(count, modeling.getAllCombinedModels().size());
        while (count < 10) {
            model = getCombinedModel();
            modeling.pushCombinedModel(model);
            count++;
            assertEquals(count, modeling.getAllCombinedModels().size());
        }
    }

    @Test
    void popCombinedModel() {
        CombinedModel model;
        int count = 0;
        assertEquals(count, modeling.getAllCombinedModels().size());
        while (count < 10) {
            model = getCombinedModel();
            modeling.pushCombinedModel(model);
            count++;
            assertEquals(count, modeling.getAllCombinedModels().size());
        }
        assertEquals("model-" + count, modeling.getCurrentModel().getId());
        for (int i = 0; i < 3; i++) {
            modeling.popCombinedModel();
            count--;
        }
        assertEquals("model-" + count, modeling.getCurrentModel().getId());
    }

    @Test
    void popCombinedModelOnEmptyStack() {
        assertThrows(UnsupportedOperationException.class,
                () -> modeling.popCombinedModel());

        // we add a model and see if the pointer behaves naturally
        CombinedModel model = getCombinedModel();
        modeling.pushCombinedModel(model);

        assertEquals(model, modeling.getCurrentModel());

    }

    @Test
    void pushCombinedModelOnReducedStackPointer() {
        CombinedModel model;
        int count = 0;
        assertEquals(count, modeling.getAllCombinedModels().size());
        while (count < 10) {
            model = getCombinedModel();
            modeling.pushCombinedModel(model);
            count++;
            assertEquals(count, modeling.getAllCombinedModels().size());
        }
        assertEquals("model-" + count, modeling.getCurrentModel().getId());
        for (int i = 0; i < 3; i++) {
            modeling.popCombinedModel();
            count--;
        }
        assertEquals("model-" + count, modeling.getCurrentModel().getId());
        CombinedModel intermediateModel = getCombinedModel();

        modeling.pushCombinedModel(intermediateModel);
        count++;
        assertEquals(count, modeling.getAllCombinedModels().size());
        assertEquals(intermediateModel, modeling.getCurrentModel());

        // ensure we are actually a max stack position
        assertThrows(UnsupportedOperationException.class,
                () -> modeling.restoreCombinedModel());
    }


    @Test
    void restoreCombinedModel() {
        CombinedModel model;
        int count = 0;
        assertEquals(count, modeling.getAllCombinedModels().size());
        while (count < 10) {
            model = getCombinedModel();
            modeling.pushCombinedModel(model);
            count++;
            assertEquals(count, modeling.getAllCombinedModels().size());
        }
        assertEquals("model-" + count, modeling.getCurrentModel().getId());
        for (int i = 0; i < 3; i++) {
            modeling.popCombinedModel();
            count--;
        }
        assertEquals("model-" + count, modeling.getCurrentModel().getId());
        for (int i = 0; i < 3; i++) {
            modeling.restoreCombinedModel();
            count++;
        }
        assertEquals("model-" + count, modeling.getCurrentModel().getId());
    }

    @Test
    void restoreCombinedModelOnMaxStackPointer() {
        CombinedModel model;
        int count = 0;
        assertEquals(count, modeling.getAllCombinedModels().size());
        while (count < 10) {
            model = getCombinedModel();
            modeling.pushCombinedModel(model);
            count++;
            assertEquals(count, modeling.getAllCombinedModels().size());
        }
        assertThrows(UnsupportedOperationException.class, () -> modeling.restoreCombinedModel());
    }


    private CombinedModel getCombinedModel() {
        SchemaNode root = new ObjectNode("root");

        SchemaNode node = new PrimitiveNode(
                "node-" + counter++,
                DataType.Unknown
        );
        Edge edge = new Edge(root.getUuid(), node.getUuid());
        SyntaxModel syntaxModel = new SyntaxModel(root.getUuid(), new ArrayList<>(Arrays.asList(root, node)), new ArrayList<>(Collections.singletonList(edge)));

        Class sClass = new Class("plasma:concept" + counter++,
                "class-" + counter,
                "description"
        );
        SemanticModel semanticModel = new SemanticModel(null, new ArrayList<>(Collections.singletonList(sClass)), new ArrayList<>());

        return new CombinedModel("model-" + modelcounter++, syntaxModel, semanticModel);
    }
}