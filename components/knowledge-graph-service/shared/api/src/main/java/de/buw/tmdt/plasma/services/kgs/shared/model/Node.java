package de.buw.tmdt.plasma.services.kgs.shared.model;

import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.jetbrains.annotations.NotNull;

public abstract class Node extends Element {

	private static final long serialVersionUID = 6635019232586170812L;

	protected Node(@NotNull String id) {
		super(id);
	}

	@Override
	public String toString() {
		return this.toPropertyValuePairStringBuilder().toString();
	}

	@Override
	protected StringUtilities.PropertyValuePairStringBuilder toPropertyValuePairStringBuilder() {
		return super.toPropertyValuePairStringBuilder();
	}
}