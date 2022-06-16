package de.buw.tmdt.plasma.services.dms.core.operations.impl.misc;

import de.buw.tmdt.plasma.datamodel.modification.operation.DataType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BooleanHomogenizer extends AbstractSyntaxHomogenizer {
	private static final Logger logger = LoggerFactory.getLogger(BooleanHomogenizer.class);

	private static final String[] FALSE_CANDIDATES = {"f", "false", "0"};
	private static final String[] TRUE_CANDIDATES = {"t", "true", "1"};

	private static final String FALSE_CONSTANT = "false";
	private static final String TRUE_CONSTANT = "true";

	public BooleanHomogenizer() {
		super(DataType.Boolean);
	}

	public static boolean parseHomogenizedRepresentation(@NotNull String homogenizedValue) throws DataTypeException {
		for (String falseConstant : FALSE_CANDIDATES) {
			if (falseConstant.equalsIgnoreCase(homogenizedValue)) {
				return false;
			}
		}

		for (String trueConstant : TRUE_CANDIDATES) {
			if (trueConstant.equalsIgnoreCase(homogenizedValue)) {
				return true;
			}
		}

		throw new DataTypeException(
				"Passed raw value \"" + homogenizedValue + "\" was neither \"" +
						BooleanHomogenizer.TRUE_CONSTANT + "\" nor \"" +
						BooleanHomogenizer.FALSE_CONSTANT + "\"."
		);
	}

	/**
	 * Valid input values for {@link BooleanHomogenizer#TRUE_CONSTANT}/{@link BooleanHomogenizer#FALSE_CONSTANT} are
	 * {@link BooleanHomogenizer#TRUE_CANDIDATES}/{@link BooleanHomogenizer#FALSE_CANDIDATES}.
	 *
	 * @param value    the value of which the platform specific representation is required
	 * @param dataType the data type of the value
	 *
	 * @return {@link BooleanHomogenizer#TRUE_CONSTANT} or {@link BooleanHomogenizer#FALSE_CONSTANT}
	 *
	 * @throws HomogenizationException       if the value could not be parsed
	 * @throws DataTypeNotSupportedException if the data type is not supported by the homogenizer
	 */
	@Override
	public String homogenizeRepresentation(@NotNull String value, @NotNull DataType dataType) throws HomogenizationException, DataTypeNotSupportedException {
		assertDataTypeValidity(dataType, BooleanHomogenizer.class.getName());

        try {
            if (BooleanHomogenizer.parseHomogenizedRepresentation(value)) {
                return TRUE_CONSTANT;
            } else {
                return FALSE_CONSTANT;
            }
        } catch (DataTypeException ignored) {
            logger.trace("Failed to match the value against any of the known constants.");
        }

        logger.trace("Value did not match (case insensitively) any of the known constants for false: {}.", (Object) FALSE_CANDIDATES);
        try {
            if (Double.parseDouble(value) == 0) {
                return BooleanHomogenizer.FALSE_CONSTANT;
            }
            return BooleanHomogenizer.TRUE_CONSTANT;
        } catch (NumberFormatException e) {
            logger.trace("Failed to parse the value as double to do a comparison to 0.", e);
        }
        throw new HomogenizationException("unknown boolean type representation");
    }
}