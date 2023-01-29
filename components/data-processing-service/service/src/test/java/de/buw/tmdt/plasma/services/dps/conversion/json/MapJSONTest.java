package de.buw.tmdt.plasma.services.dps.conversion.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("checkstyle:LocalVariableName")
public class MapJSONTest {
    @SuppressWarnings("HardcodedFileSeparator")
    private static final String JSON_EXTRACTED_RESOURCE = "mappedJSON.json";
    private static final Logger log = LoggerFactory.getLogger(MapJSONTest.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    MapJSON testee;

    String[] entityTypeIds = {"et0", "et1", "et2", "et3", "et4", "et5", "et6", "et7", "et8", "et61", "IMG_0", "IMG_1"};

    @BeforeEach
    public void setUp() throws MappingException {
        //initialize mocks
        MockitoAnnotations.initMocks(this);
        this.testee = new MapJSON();
        this.testee.setAddLineDelimiter("true");

        TreeNode mapping = getMappingTree();
        this.testee.setStructure(mapping);
    }

    private TreeNode getMappingTree() throws MappingException {
        TreeNode root = new TreeNode(null, "root", null, null);

        // single number
        TreeNode root_number0 = new TreeNode(null, "root_number0", entityTypeIds[0], null);
        root.addChild(root_number0);

        // single strings
        TreeNode root_string0 = new TreeNode(null, "root_string0", entityTypeIds[4], null);
        root.addChild(root_string0);
        TreeNode root_string1 = new TreeNode(null, "root_string1", entityTypeIds[5], null);
        root.addChild(root_string1);

        // null field
        TreeNode root_null = new TreeNode(null, "root_null", null, null);
        root.addChild(root_null);

        // nested object with same strings
        TreeNode root_object0 = new TreeNode(null, "root_object0", null, null);
        TreeNode root_object0_string0 = new TreeNode(null, "root_object0_string0", entityTypeIds[4], null);
        root_object0.addChild(root_object0_string0);
        TreeNode root_object0_string1 = new TreeNode(null, "root_object0_string1", entityTypeIds[5], null);
        root_object0.addChild(root_object0_string1);
        root.addChild(root_object0);

        // nested array with strings
        TreeNode root_array0 = new TreeNode(null, "root_array0", null, null, true);
        TreeNode root_array0_string0 = new TreeNode(null, "ignored", entityTypeIds[4], null);
        root_array0.addChild(root_array0_string0);
        TreeNode root_array0_string1 = new TreeNode(null, "ignored", entityTypeIds[5], null);
        root_array0.addChild(root_array0_string1);
        root.addChild(root_array0);

        // nested array with objects (both objects have to be of equal structure here, due to validation)
        // in theory, we can also create heterogeneous objects inside a single array
        TreeNode root_array1 = new TreeNode(null, "root_array1", null, null, true);
        TreeNode root_array1_Object0 = new TreeNode(null, "ignored", null, null);
        TreeNode root_array1_Object0_string0 = new TreeNode(null, "root_array1_Object_string0", entityTypeIds[4], null);
        root_array1_Object0.addChild(root_array1_Object0_string0);
        root_array1.addChild(root_array1_Object0);
        TreeNode root_array1_Object1 = new TreeNode(null, "ignored", null, null);
        TreeNode root_array1_Object1_string0 = new TreeNode(null, "root_array1_Object_string0", entityTypeIds[4], null);
        root_array1_Object1.addChild(root_array1_Object1_string0);
        root_array1.addChild(root_array1_Object1);
        root.addChild(root_array1);

        // nested array with arrays of primitive
        // in theory, we can also create heterogeneous objects inside a single array
        TreeNode root_array2 = new TreeNode(null, "root_array2", null, null, true);
        TreeNode root_array2_array0 = new TreeNode(null, "ignored", null, null, true);
        TreeNode root_array2_array0_string0 = new TreeNode(null, "ignored", entityTypeIds[4], null);
        root_array2_array0.addChild(root_array2_array0_string0);
        root_array2.addChild(root_array2_array0);
        TreeNode root_array2_array1 = new TreeNode(null, "ignored", null, null, true);
        TreeNode root_array2_array1_string0 = new TreeNode(null, "ignored", entityTypeIds[4], null);
        root_array2_array1.addChild(root_array2_array1_string0);
        root_array2.addChild(root_array2_array1);
        root.addChild(root_array2);

        return root;

    }

    /*

    @Test
    public void testMapJson() throws URISyntaxException, EskapeTaskException, IOException {
        SLTObject sltObjectInput = SLTObjectFactory.getIntegratedObject();

        log.trace("Create Data to execute.");
        Data data = new Data();
        data.addField(OUTPUT_PROCESSING_PAYLOAD, sltObjectInput);

        log.trace("Execute Task.");
        testee.execute(data);

        log.trace("Execute Task.");
        testee.execute(data);

        log.trace("Verify the Output is emitted.");
        verify(dataEmitter, times(2)).emitData(dataArgumentCaptor.capture());

        String formatted = (String) dataArgumentCaptor.getValue().getValue(OUTPUT_EXTRACTION_FORMATTED_DATA);
        assertNotNull(formatted);

        ObjectNode formattedJson = (ObjectNode) objectMapper.readTree(formatted);
        ObjectNode expectedJson = (ObjectNode) objectMapper.readTree(
                ResourceHelper.getResourceContentString(JSON_EXTRACTED_RESOURCE));

        assertNotNull(formattedJson);
        log.info(expectedJson.toString());
        log.info(formattedJson.toString());

        //verify that same as expected
        assertTrue(JSONComparisonHelper.areEqualElements(formattedJson, expectedJson));

        log.trace("Verify there are no other additional fields in EmittedData.");
        Output outputExpected = testee.getClass().getAnnotation(Output.class);
        assertEquals(outputExpected.value().length, dataArgumentCaptor.getValue().getFields().size());
    }

    @Test
    public void testFailedMapping() throws EskapeTaskException, MappingException {
        SLTObject sltObjectInput = SLTObjectFactory.getIntegratedObject();

        Data data = new Data();
        data.addField(OUTPUT_PROCESSING_PAYLOAD, sltObjectInput);

        TreeNode structure;

        // wrong mapping for number node
        structure = getMappingTree();
        structure.getChildren().get(0).setMappedEntityId("nonExistentEntityId");
        setStructure(structure);
        assertThrows(EskapeTaskException.class, () -> testee.execute(data));

        // duplicate key in object
        structure = getMappingTree();
        TreeNode root_object0 = structure.getChildren().get(4);
        TreeNode root_object0_child0 = root_object0.getChildren().get(0);
        TreeNode root_object0_newChild = new TreeNode(
                null,
                root_object0_child0.getLabel(),
                root_object0_child0.getMappedEntityId(),
                null);
        root_object0.addChild(root_object0_newChild);
        setStructure(structure);
        assertThrows(EskapeTaskException.class, () -> testee.execute(data));

        // array with different type of children
        structure = getMappingTree();
        TreeNode arrayChild0 = structure.getChildren().get(5).getChildren().get(0);
        arrayChild0.setArrayRoot(true); // define one child as array
        arrayChild0.setMappedEntityId(null);
        TreeNode rogueChild = new TreeNode(null, "rogueChild", entityTypeIds[4], null);
        arrayChild0.addChild(rogueChild);
        setStructure(structure);
        assertThrows(EskapeTaskException.class, () -> testee.execute(data));
    }

    private void setStructure(TreeNode structure) throws EskapeTaskException {
        ObjectMapper mapper = new ObjectMapper();
        String entityMappingS = "";
        try {
            entityMappingS = mapper.writeValueAsString(structure);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        testee.setStructure(entityMappingS);
    }

    @SuppressWarnings("Duplicates")
    @Test
    public void testMorePacketsJSON() throws URISyntaxException, EskapeTaskException, IOException {
        int numberOfPackets = 2;
        //receive packets
        SLTObject sltObjectInput = SLTObjectFactory.getIntegratedObject();

        for (int i = 0; i < numberOfPackets; i++) {
            log.trace("Create Data to execute.");
            Data data = new Data();
            data.addField(OUTPUT_PROCESSING_PAYLOAD, sltObjectInput);

            log.trace("Execute Task.");
            testee.execute(data);
        }

        log.trace("Verify the Output is emitted.");
        verify(dataEmitter, times(numberOfPackets)).emitData(dataArgumentCaptor.capture());

        for (Data data : dataArgumentCaptor.getAllValues()) {
            String formatted = (String) data.getValue(OUTPUT_EXTRACTION_FORMATTED_DATA);

            ObjectNode formattedJson = (ObjectNode) objectMapper.readTree(formatted);
            ObjectNode object = (ObjectNode) objectMapper.readTree(ResourceHelper.getResourceContentString(JSON_EXTRACTED_RESOURCE));

            assertNotNull(formattedJson);

            //verify that same as expected
            assertTrue(JSONComparisonHelper.areEqualElements(object, formattedJson));

            Output outputExpected = testee.getClass().getAnnotation(Output.class);
            assertEquals(outputExpected.value().length, dataArgumentCaptor.getValue().getFields().size());
        }
    }

     */
}