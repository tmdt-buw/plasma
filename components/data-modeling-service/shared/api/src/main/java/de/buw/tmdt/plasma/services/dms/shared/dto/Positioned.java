package de.buw.tmdt.plasma.services.dms.shared.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public class Positioned implements Serializable {
	public static final String X_COORDINATE_PROPERTY = "x";
	public static final String Y_COORDINATE_PROPERTY = "y";
	private static final long serialVersionUID = -3619721225687115309L;

	private final Double xCoordinate;
	private final Double yCoordinate;

	@JsonCreator
	public Positioned(
			@Nullable @JsonProperty(X_COORDINATE_PROPERTY) Double xCoordinate,
			@Nullable @JsonProperty(Y_COORDINATE_PROPERTY) Double yCoordinate
	) {
		this.xCoordinate = xCoordinate;
		this.yCoordinate = yCoordinate;
	}

	@Nullable
	@JsonProperty(X_COORDINATE_PROPERTY)
	public Double getXCoordinate() {
		return xCoordinate;
	}

	@Nullable
	@JsonProperty(Y_COORDINATE_PROPERTY)
	public Double getYCoordinate() {
		return yCoordinate;
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"Positioned\""
		       + ",\"xCoordinate\":\"" + xCoordinate + '"'
		       + ",\"yCoordinate\":\"" + yCoordinate + '"'
		       + '}';
	}
}
