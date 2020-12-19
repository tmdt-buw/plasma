package de.buw.tmdt.plasma.services.kgs.shared.model;

import de.buw.tmdt.plasma.utilities.misc.StringUtilities;

import java.io.Serializable;
import java.util.Objects;

public class Position implements Serializable {

	private static final long serialVersionUID = 3615108777410497552L;

	private final double x;
	private final double y;

	public Position(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Position)) {
			return false;
		}
		Position position = (Position) o;
		return Double.compare(position.x, x) == 0 &&
		       Double.compare(position.y, y) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	@Override
	public String toString() {
		StringUtilities.PropertyValuePairStringBuilder propertyValuePairStringBuilder = new StringUtilities.PropertyValuePairStringBuilder();
		propertyValuePairStringBuilder.addPair("x", this.x);
		propertyValuePairStringBuilder.addPair("y", this.y);
		return propertyValuePairStringBuilder.toString();
	}
}