package de.buw.tmdt.plasma.services.dms.core.operations.impl.misc;


import de.buw.tmdt.plasma.datamodel.modification.operation.DataType;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractSyntaxHomogenizer implements SyntaxHomogenizer {
	protected final List<DataType> dataTypes;

	protected AbstractSyntaxHomogenizer(DataType... dataTypes) {
		this.dataTypes = Arrays.asList(dataTypes);
	}

	@Override
	public boolean acceptsDataType(DataType dataType) {
		return dataTypes.contains(dataType);
	}

	@SuppressWarnings("ReturnOfCollectionOrArrayField - fine since Arrays.asList(Object...) returns an unmodifiable list")
	@Override
	public List<DataType> getSupportedDataTypes() {
		return dataTypes;
	}

	protected void assertDataTypeValidity(DataType dataType, String homogenizerName) throws DataTypeNotSupportedException {
		if (dataTypes.contains(dataType)) {
			return;
		}
		throw new DataTypeNotSupportedException(homogenizerName + " only supports " + dataTypes + " but was invoked with " + dataType);
	}
}