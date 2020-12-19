package de.buw.tmdt.plasma.services.kgs.shared.model;

import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

public abstract class Element implements Serializable {

	private static final long serialVersionUID = 3007009066699975773L;

	private final String id;

	protected Element(@NotNull String id) {
		this.id = id;
	}

	@NotNull
	public String getId() {
		return this.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !getClass().equals(o.getClass())) {
			return false;
		}

		Element that = (Element) o;
		return Objects.equals(this.id, that.id);
	}

	@Override
	public String toString() {
		return this.toPropertyValuePairStringBuilder().toString();
	}

	protected StringUtilities.PropertyValuePairStringBuilder toPropertyValuePairStringBuilder() {
		StringUtilities.PropertyValuePairStringBuilder propertyValuePairStringBuilder = new StringUtilities.PropertyValuePairStringBuilder();
		propertyValuePairStringBuilder.addPair("id", this.id);
		return propertyValuePairStringBuilder;
	}
}