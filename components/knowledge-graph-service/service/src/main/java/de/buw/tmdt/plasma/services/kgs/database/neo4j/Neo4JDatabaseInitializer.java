package de.buw.tmdt.plasma.services.kgs.database.neo4j;

import org.jetbrains.annotations.NotNull;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class Neo4JDatabaseInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(Neo4JDatabaseInitializer.class);

    private final List<Neo4JModel.Neo4JEntity> entities = new ArrayList<>();
    private final GraphDBDriverFactoryImpl graphDBDriverFactory;
    private boolean initialized = false;

    @Autowired
    public Neo4JDatabaseInitializer(@NotNull GraphDBDriverFactoryImpl graphDBDriverFactory) {
        this.graphDBDriverFactory = graphDBDriverFactory;
    }

    public void registerEntity(@NotNull Neo4JModel.Neo4JEntity neo4JEntity) {
        entities.add(neo4JEntity);
    }

    @Override
    public void onApplicationEvent(@NotNull ContextRefreshedEvent contextRefreshedEvent) {
        if (!initialized) {
            initializeIndex();
            initialized = true;
        } else {
            logger.debug("Neo4J is already inizialized - ignoring event");
        }
    }

    private void initializeIndex() {
        logger.debug("Creating Neo4 Indices");
        int createdCount = 0;
        Session session = graphDBDriverFactory.openDriver().session();
        logger.trace("Retrieving online indexes list.");
        List<Record> indexList;
        try {
            indexList = session.run("CALL db.indexes()").list();
        } catch (RuntimeException e) {
            logger.error("Error querying neo4j indicies.", e);
            return;
        }

        try (Transaction transaction = session.beginTransaction()) {
            for (Neo4JModel.Neo4JEntity neo4JEntity : entities) {
                String indexString = String.format("INDEX ON :%s(%s)", neo4JEntity.getClassProperty(), neo4JEntity.getIndexProperty());
                boolean indexAlreadyOnline = false;
                for (Record record : indexList) {
                    if (indexString.equals(record.get("description").asString())) {
                        // Index already online.
                        indexAlreadyOnline = true;
                        break;
                    }
                }
                if (!indexAlreadyOnline) {
                    logger.trace("Index on {} is not present in Neo4j, creating.", neo4JEntity.getClassProperty());
                    //noinspection SingleCharacterStringConcatenation: This is Neo4j syntax.
                    transaction.run("CREATE INDEX ON :`"
                            + neo4JEntity.getClassProperty()
                            + "`("
                            + neo4JEntity.getIndexProperty()
                            + ")"
                    );
                    createdCount++;
                }
            }
            transaction.commit();
            if (createdCount > 0) {
                logger.debug("Created {} new Neo4j indexes, all indexes are online now.", createdCount);
            } else {
                logger.debug("All Neo4j indexes are already online.");
            }
        } catch (RuntimeException e) {
            logger.error("Error creating Neo4j indexes.", e);
        }
        session.close();
    }
}