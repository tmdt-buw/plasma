package de.buw.tmdt.plasma.services.kgs.database.neo4j;

import de.buw.tmdt.plasma.services.kgs.database.api.GraphDBDriver;
import de.buw.tmdt.plasma.services.kgs.database.api.exception.ConceptNameAlreadyExistsException;
import de.buw.tmdt.plasma.services.kgs.shared.model.knowledgegraph.EntityConcept;
import de.buw.tmdt.plasma.services.kgs.shared.model.knowledgegraph.RelationConcept;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Service
@Profile("initialize-database")
public class Neo4JDataInitializer {

	private static final Logger logger = LoggerFactory.getLogger(Neo4JDataInitializer.class);

	@SuppressWarnings("HardcodedFileSeparator - OK since it is a resource file")
	private static final String ENTITY_CONCEPT_CONFIG_FILE = "neo4j/entityConcepts.cfg";
	@SuppressWarnings("HardcodedFileSeparator - OK since it is a resource file")
	private static final String RELATION_CONCEPT_CONFIG_FILE = "neo4j/relationConcepts.cfg";
	private static final String CONFIG_FIELD_SEPARATOR = ",";
	private static final String CONFIG_LIST_SEPARATOR = ";";
	@SuppressWarnings("HardcodedFileSeparator")
	private static final String COMMENT_PREFIX = "//";

	private final GraphDBDriverFactoryImpl graphDBDriverFactory;

	@Autowired
	public Neo4JDataInitializer(
			GraphDBDriverFactoryImpl graphDBDriverFactory
	) {
		this.graphDBDriverFactory = graphDBDriverFactory;
	}

	@PostConstruct
	public void initializeData() {
		logger.info("Initializing Neo4J GraphDB");
		graphDBDriverFactory.openDriver();
		graphDBDriverFactory.executeCypher("MATCH (n) DETACH DELETE n");
		graphDBDriverFactory.closeDriver();

		GraphDBDriver graphDBDriver = graphDBDriverFactory.getGraphDBDriver();

		try (BufferedReader reader = new BufferedReader(getResourceReader(ENTITY_CONCEPT_CONFIG_FILE))) {
			logger.info("Preparing EntityConcepts.");
			reader.lines()
					//filter lines which start with //
					.filter(line -> !line.startsWith(COMMENT_PREFIX))
					//split line into fields
					.map(line -> line.split(CONFIG_FIELD_SEPARATOR))
					//map tokens to EntityConcept
					.map(tokens -> new EntityConcept("", tokens[0], tokens.length > 1 && tokens[1] != null ? tokens[1] : ""))
					.forEach(entityConcept -> {
						try {
							graphDBDriver.createEntityConcept(entityConcept);
						} catch (ConceptNameAlreadyExistsException e) {
							logger.debug(e.getMessage());
						}
					});
		} catch (IOException e) {
			logger.error("Unexpected Exception - Could not initialize Neo4J Entity Concepts", e);
		}

		try (BufferedReader reader = new BufferedReader(getResourceReader(RELATION_CONCEPT_CONFIG_FILE))) {
			logger.info("Preparing RelationConcepts.");
			reader.lines()
					//removes lines which start with //
					.filter(line -> !line.startsWith(COMMENT_PREFIX))
					//split line into fields
					.map(line -> line.split(CONFIG_FIELD_SEPARATOR))
					.map(tokens -> {
						String name = tokens[0];
						String[] properties = tokens.length > 1 && tokens[1] != null ? tokens[1].split(CONFIG_LIST_SEPARATOR) : new String[0];
						String description = tokens.length > 2 && tokens[2] != null ? tokens[2] : "";

						Set<RelationConcept.Property> relConceptProperties = new HashSet<>();
						try {
							relConceptProperties = RelationConcept.Property.byClassNames(Arrays.asList(properties));
						} catch (RuntimeException e) {
							logger.warn("unable to load properties of relation concepts for '{}', setting no properties", name);
						}

						return new RelationConcept("", name, description, relConceptProperties);
					})
					//create triple of name, description and properties
					.forEach(relationConcept -> {
						try {
							graphDBDriver.createRelationConcept(relationConcept);
						} catch (ConceptNameAlreadyExistsException e) {
							logger.debug(e.getMessage());
						}
					});
		} catch (IOException e) {
			logger.error("Unexpected Exception - Could not initialize Neo4J Relation Concepts", e);
		}
	}

	@NotNull
	private InputStreamReader getResourceReader(@NotNull String resourceName) {
		return new InputStreamReader(
				Objects.requireNonNull(Neo4JDataInitializer.class.getClassLoader().getResourceAsStream(resourceName)),
				StandardCharsets.UTF_8
		);
	}
}