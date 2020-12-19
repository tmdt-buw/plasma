package de.buw.tmdt.plasma.services.sas.core.basic;

import de.buw.tmdt.plasma.services.sas.core.model.syntaxmodel.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PerformSchemaAnalysisTest {
	private static final Logger logger = LoggerFactory.getLogger(PerformSchemaAnalysisTest.class);
	@SuppressWarnings("HardcodedFileSeparator")
	private static final String ROOT_DIRECTORY = "basic/";
	private PerformSchemaAnalysis testee;

	private Pattern pattern = Pattern.compile("\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b");

	@BeforeEach
	void setUp() {
		MockitoAnnotations.initMocks(this);
		testee = new PerformSchemaAnalysis();
	}

	@Test
	void testRecognizedSchemaNodeCreation() throws IOException, URISyntaxException {
		runStructureTest("generalJsonElement.json", "generalJsonElementExpected.json");
	}

	@Test
	void testSimpleObject() throws IOException, URISyntaxException {
		runStructureTest("simpleObject.json", "simpleObjectExpected.json");
	}

	@Test
	void testListPrimitiveObject() throws IOException, URISyntaxException {
		runStructureTest("listPrimitiveObject.json", "listPrimitiveObjectExpected.json");
	}

	private void runStructureTest(
			String translatedDataFile,
			String expectedSchemaFile
	) throws IOException, URISyntaxException {

		String expected = ResourceHelper.getResourceContentString(ROOT_DIRECTORY + expectedSchemaFile);
		Node result = testee.performSchemaAnalysis(ResourceHelper.getResourceContentString(ROOT_DIRECTORY + translatedDataFile));


		logger.info("RESULT");
		logger.error(removeIds(result.toString()));
		assertEquals(getComparableString(expected), getComparableString(result.toString()));
	}


	private String getComparableString(String original) {
		return flattenString(removeIds(original));
	}

	private String removeIds(String original) {
		return pattern.matcher(original).replaceAll("null");
	}

	private String flattenString(String original) {
		return original.replaceAll("\n", "").replaceAll("\r", "").replaceAll("\t", "").trim();
	}
}
