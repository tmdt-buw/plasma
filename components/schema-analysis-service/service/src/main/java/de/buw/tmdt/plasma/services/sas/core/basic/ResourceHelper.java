package de.buw.tmdt.plasma.services.sas.core.basic;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class ResourceHelper {
	private static final ClassLoader classLoader = ResourceHelper.class.getClassLoader();

	private ResourceHelper() { // prevent call construction of utility class
	}

	/**
	 * Reads the content of a file from the resources as String encoded by the platforms default encoding.
	 *
	 * @param resourceName the name of the resource
	 *
	 * @return the content of the file
	 */
	public static String getResourceContentString(String resourceName) throws IOException, URISyntaxException, FileNotFoundException {
		return IOUtils.toString(getResourceReader(resourceName));
	}

	/**
	 * Provides the content of a file from the resources as InputStreamReader encoded by the platforms default encoding.
	 *
	 * @param resourceName the name of the resource
	 *
	 * @return the content of the file
	 */
	private static InputStreamReader getResourceReader(String resourceName) throws URISyntaxException, FileNotFoundException {
		return new InputStreamReader(
				new FileInputStream(
						getResourceFile(resourceName)
				),
				StandardCharsets.UTF_8
		);
	}

	/**
	 * Provides a File for the given resource name.
	 *
	 * @param resourceName the name of the resource
	 *
	 * @return the File of the given name
	 */
	private static File getResourceFile(String resourceName) throws URISyntaxException {
		final URL resourceURL = classLoader.getResource(resourceName);
		if (resourceURL == null) {
			throw new IllegalArgumentException("Can not find resource: `" + resourceName + "`.");
		}
		return new File(resourceURL.toURI());
	}
}