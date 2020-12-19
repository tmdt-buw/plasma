package de.buw.tmdt.plasma.services.kgs.database.neo4j;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@ConfigurationProperties(prefix = "neo4j")
public class Neo4JProfile implements Serializable {

	private static final char HOST_PORT_SEPARATOR = ':';

	private static final long serialVersionUID = 5363341806287921361L;
	private String host;
	private String username;
	private String password;
	private String boltPort = "7687";

	/**
	 * Defines the maximum sessions the driver will open to a single database (c.f.
	 * <a href="neo4j.com/docs/api/java-driver/current/org/neo4j/driver/v1/Config.ConfigBuilder.html#withMaxConnectionPoolSize-int-">doc</a>).
	 * It can be overwritten via the projects configuration file as {@code neo4j.maxIdleSessions}.
	 */
	@SuppressWarnings("MagicNumber")
	private int maxIdleSessions = 100;

	/**
	 * Defines the data base connection timeout.
	 * It can be overwritten via the projects configuration file as {@code neo4j.timeoutMs}.
	 */
	@SuppressWarnings("MagicNumber - default value which is overwritten if provided via configuration file")
	private int timeoutMs = 5000;

	public int getTimeoutMs() {
		return timeoutMs;
	}

	public void setTimeoutMs(int timeoutMs) {
		this.timeoutMs = timeoutMs;
	}

	public int getMaxIdleSessions() {
		return maxIdleSessions;
	}

	public void setMaxIdleSessions(int maxIdleSessions) {
		this.maxIdleSessions = maxIdleSessions;
	}

	@NotNull
	public String getHost() {
		return host;
	}

	public void setHost(@NotNull String host) {
		this.host = host;
	}

	@NotNull
	public String getUsername() {
		return username;
	}

	public void setUsername(@NotNull String username) {
		this.username = username;
	}

	@NotNull
	public String getPassword() {
		return password;
	}

	public void setPassword(@NotNull String password) {
		this.password = password;
	}

	@NotNull
	public String getBoltPort() {
		return boltPort;
	}

	public void setBoltPort(@NotNull String boltPort) {
		this.boltPort = boltPort;
	}

	@NotNull
	public String getUri() {
		return "bolt://" + host + HOST_PORT_SEPARATOR + boltPort;
	}
}
