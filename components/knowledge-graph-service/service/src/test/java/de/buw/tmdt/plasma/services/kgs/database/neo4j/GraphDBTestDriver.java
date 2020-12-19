package de.buw.tmdt.plasma.services.kgs.database.neo4j;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.io.GraphReader;
import org.apache.tinkerpop.gremlin.structure.io.graphml.GraphMLReader;
import org.apache.tinkerpop.gremlin.structure.io.graphml.GraphMLWriter;
import org.neo4j.driver.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;

public class GraphDBTestDriver extends GraphDBDriverImpl {

	private static final Logger logger = LoggerFactory.getLogger(GraphDBTestDriver.class);
	private static final ClassLoader classLoader = GraphDBTestDriver.class.getClassLoader();

	public GraphDBTestDriver(Driver driver) {
		super(driver);
	}

	/**
	 * Loads a file with a given Name from the resource folder that belongs to the current classLoader.
	 *
	 * @param filename The name of the file that we want to load.
	 *
	 * @return The absolute filePath of the file or NULL if the file was not found
	 */
	private static String getPathOfFile(String filename) throws URISyntaxException {
		return new File(classLoader.getResource(filename).toURI()).toString();
	}

	public void loadDatabaseDump(String dbResource) {
		final GraphReader reader = GraphMLReader.build().create();
		Graph graph = getGraph();
		try (final InputStream stream = new FileInputStream(GraphDBTestDriver.getPathOfFile(dbResource))) {
			reader.readGraph(stream, graph);
		} catch (FileNotFoundException e) {
			logger.error("unable to find file", e);
		} catch (IOException e) {
			logger.error("unable to open file", e);
		} catch (URISyntaxException e) {
			logger.error("invalid uri syntax", e);
		}
		closeGraph(graph);
	}

	@SuppressWarnings({"unused - May be used in test classes on demand.", "WeakerAccess"})
	public void exportGraphDump(String filepath) {
		// Export the current database state as a tinkerpop dump.
		final File targetFile = new File(filepath);
		final Graph graph = getGraph();
		logger.info("Writing graph to file.");
		try (final OutputStream os = new FileOutputStream(targetFile)) {
			logger.info("Output file path: {}", targetFile.getAbsolutePath());
			GraphMLWriter.build().create().writeGraph(os, graph);
			logger.info("Done.");
		} catch (IOException e) {
			logger.error("Could not store dump", e);
		}
		closeGraph(graph);
	}
}