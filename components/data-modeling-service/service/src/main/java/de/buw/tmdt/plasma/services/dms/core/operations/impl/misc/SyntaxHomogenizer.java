package de.buw.tmdt.plasma.services.dms.core.operations.impl.misc;

import de.buw.tmdt.plasma.datamodel.modification.operation.DataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface SyntaxHomogenizer {
	/**
	 * Returns a platform specific representation for the passed {@code value} and {@code dataType}. The {@code dataType} determines the approaches which are
	 * taken to derive the representation. If the value is malformed or the {@code dataType} is not supported by the implementation the method throws a {@code
	 * HomogenizationException}
	 *
	 * @param value    the value of which the platform specific representation is required
	 * @param dataType the data type of the value
	 *
	 * @return the platform specific representation of the provided value
	 *
	 * @throws HomogenizationException       if no applicable approach was found to convert the value
	 * @throws DataTypeNotSupportedException if the provided {@link DataType} is not supported by the implementation
	 */
	String homogenizeRepresentation(String value, @NotNull DataType dataType) throws HomogenizationException, DataTypeNotSupportedException;

	/**
	 * Returns true if the provided {@code dataType} is supported by the implementation. If this returns {@code true} for a given {@code dataType} the
	 * {@link #homogenizeRepresentation(String, DataType)} method must never throw a {@link DataTypeNotSupportedException} for that {@code dataType}.
	 *
	 * @param dataType the dataType for which the support is checked
	 *
	 * @return true iff the implementation does support the given {@link DataType}
	 */
	boolean acceptsDataType(DataType dataType);

	/**
	 * Returns a list of all supported {@link DataType DataTypes}. The contract of {@link #acceptsDataType(DataType)} applies for each element of the returned
	 * list.
	 *
	 * @return a list of all supported {@link DataType DataTypes}
	 */
	List<DataType> getSupportedDataTypes();
}