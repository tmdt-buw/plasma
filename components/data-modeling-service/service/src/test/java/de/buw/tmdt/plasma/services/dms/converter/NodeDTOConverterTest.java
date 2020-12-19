package de.buw.tmdt.plasma.services.dms.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.buw.tmdt.plasma.services.dms.core.converter.SchemaAnalysisDTOConverter;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.syntaxmodel.Node;
import de.buw.tmdt.plasma.services.sas.shared.dto.syntaxmodel.NodeDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SchemaAnalysisDTOConverterTest {

	private static final Pattern NEWLINE_N_PATTERN = Pattern.compile("\n");
	private static final Pattern NEWLINE_R_PATTERN = Pattern.compile("\r");
	private static final Pattern TAB_PATTERN = Pattern.compile("\t");
	private static final Pattern SPACE_PATTERN = Pattern.compile(" ");
	private SchemaAnalysisDTOConverter testee;
	@SuppressWarnings("HardcodedFileSeparator")
	private static final String EXPECTED_FILE = "expectedNode.json";
	@SuppressWarnings("HardcodedFileSeparator")
	private static final String INPUT_FILE = "incomingNode.json";

	private Pattern pattern = Pattern.compile("\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b");

	@BeforeEach
	void setUp() {
		testee = new SchemaAnalysisDTOConverter();
	}

	@Test
	void testNodeConversion() throws IOException {
		String expected = new String(Objects.requireNonNull(getClass().getClassLoader()
				                                                    .getResourceAsStream(EXPECTED_FILE)).readAllBytes(), StandardCharsets.UTF_8);
		String input = new String(Objects.requireNonNull(getClass().getClassLoader()
				                                                 .getResourceAsStream(INPUT_FILE)).readAllBytes(), StandardCharsets.UTF_8);

		ObjectMapper objectMapper = new ObjectMapper();

		NodeDTO nodeDTO = objectMapper.readValue(input, NodeDTO.class);

		Node actual = testee.fromNodeDTO(nodeDTO);

		assertEquals(getComparableString(expected), getComparableString(actual.toString()));
	}

	private String getComparableString(String original) {
		return flattenString(removeIds(original));
	}

	private String removeIds(String original) {
		return pattern.matcher(original).replaceAll("null");
	}

	private String flattenString(String original) {
		return SPACE_PATTERN.matcher(TAB_PATTERN.matcher(NEWLINE_R_PATTERN.matcher(NEWLINE_N_PATTERN.matcher(original).replaceAll("")).replaceAll("")).replaceAll(
				"")).replaceAll("").trim();
	}
}