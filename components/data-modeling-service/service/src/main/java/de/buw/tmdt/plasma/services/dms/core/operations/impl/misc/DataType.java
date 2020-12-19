package de.buw.tmdt.plasma.services.dms.core.operations.impl.misc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DataType {
	BOOLEAN("Boolean"),
	STRING("String"),
	NUMBER("Number"),
	BINARY("Binary");

	private final String serialization;

	DataType(String serialization) {
		this.serialization = serialization;
	}

	@JsonCreator
	public static DataType getDeserialization(String serialization) {
		for (DataType dataType : DataType.values()) {
			if (dataType.serialization.equals(serialization)) {
				return dataType;
			}
		}
		throw new InvalidDataTypeException("Can not find " + DataType.class.getName() + " instance for serialization String: \"" + serialization + "\".");
	}

	@JsonValue
	public String getSerialization() {
		return this.serialization;
	}
}