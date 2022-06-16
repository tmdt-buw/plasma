package de.buw.tmdt.plasma.services.dms.core.operations.impl.misc;

import de.buw.tmdt.plasma.datamodel.modification.operation.DataType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

public class NumberHomogenizer extends AbstractSyntaxHomogenizer {
	private static final Logger logger = LoggerFactory.getLogger(NumberHomogenizer.class);
	private static final String THROW_PARSING_FAILURE_MESSAGE = "Failed to parse value \"%s\" at index %s.";
	private static final String COULD_NOT_CONSUME_COMPLETE_VALUE_MESSAGE = THROW_PARSING_FAILURE_MESSAGE + " Intermediate result was \"%s\".";
	private final NumberFormat numberFormat;

	public NumberHomogenizer() {
        super(DataType.Number);
        this.numberFormat = NumberFormat.getNumberInstance(Locale.ENGLISH);
    }

	public static double parseHomogenizedRepresentation(@NotNull String homogenizedValue) throws DataTypeException {
		try {
			return Double.parseDouble(homogenizedValue);
		} catch (NumberFormatException e) {
			throw new DataTypeException(e);
		}
	}

	/**
	 * The homogenizer tries two approaches. The first is to simply apply {@link Double#parseDouble(String)}. The second is
	 * {@code NumberFormat.getNumberInstance(Locale.ENGLISH).parse(...)}.
	 *
	 * @param value    the value of which the platform specific representation is required
	 * @param dataType the data type of the value
	 *
	 * @return the platform specific representation of the provided value
	 *
	 * @throws HomogenizationException       if no applicable approach was found to convert the value
	 * @throws DataTypeNotSupportedException if the provided {@link DataType} is not supported by the implementation
	 */
	@Override
	public synchronized String homogenizeRepresentation(@NotNull String value, @NotNull DataType dataType) throws HomogenizationException,
			DataTypeNotSupportedException {
		assertDataTypeValidity(dataType, NumberHomogenizer.class.getName());
		try {
			return String.valueOf(NumberHomogenizer.parseHomogenizedRepresentation(value));
		} catch (DataTypeException e) {
			logger.debug("Value \"{}\" was not parsable as double.", value, e);
		}

		//second approach: try NumberFormat
		ParsePosition parsePosition = new ParsePosition(0);
		//in addition to NumberFormat.parse(String, ParsePosition) read the documentation of Format.parseObject(String, ParsePosition)
		Number number = numberFormat.parse(value, parsePosition);
		if (number == null) {
			throw new HomogenizationException(String.format(THROW_PARSING_FAILURE_MESSAGE, value, parsePosition.getErrorIndex()));
		}
		if (parsePosition.getIndex() != value.length()) {
			throw new HomogenizationException(String.format(COULD_NOT_CONSUME_COMPLETE_VALUE_MESSAGE, value, parsePosition.getIndex(), number));
		}
		return number.toString();
	}
}