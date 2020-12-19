package de.buw.tmdt.plasma.services.kgs.shared.model;

import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class Edge<O extends Node, I extends Node> extends Element {

	private static final long serialVersionUID = -8998885650649395391L;

	private final O tailNode;
	private final I headNode;

	protected Edge(@NotNull String id, @NotNull O tailNode, @NotNull I headNode) {
		super(id);
		this.tailNode = tailNode;
		this.headNode = headNode;
	}

	@NotNull
	public O getTailNode() {
		return tailNode;
	}

	@NotNull
	public I getHeadNode() {
		return headNode;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), tailNode, headNode);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Edge)) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		Edge<?, ?> edge = (Edge<?, ?>) o;
		return Objects.equals(tailNode, edge.tailNode) &&
		       Objects.equals(headNode, edge.headNode);
	}

	@Override
	public String toString() {
		return this.toPropertyValuePairStringBuilder().toString();
	}

	@Override
	protected StringUtilities.PropertyValuePairStringBuilder toPropertyValuePairStringBuilder() {
		StringUtilities.PropertyValuePairStringBuilder propertyValuePairStringBuilder = super.toPropertyValuePairStringBuilder();
		propertyValuePairStringBuilder.addPair("tailNode", this.tailNode);
		propertyValuePairStringBuilder.addPair("headNode", this.headNode);
		return propertyValuePairStringBuilder;
	}
}