package de.buw.tmdt.plasma.utilities.misc;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("WeakerAccess - no in a library class")
public final class StringUtilities {
    private static final String SCHEMA_START = "{\n";
    private static final String SCHEMA_END = "\n}";
    private static final String INDENTATION = "\t";
    private static final String PROPERTY_SEPARATOR = ",\n";

    private static final Pattern LINEBREAK_PATTERN = Pattern.compile("\n");

    private static final String JSON_ELEMENT_DELIMITER = ", ";
    private static final String JSON_OBJECT_START = "{";
    private static final String JSON_OBJECT_END = "}";
    private static final String JSON_LIST_START = "[";
    private static final String JSON_LIST_END = "]";
    private static final String JSON_KEY_QUOTE = "\"";

    private static final Pattern JSON_ESCAPING_CHARACTERS_PATTERN = Pattern.compile("([\"\\\\/])");
    private static final String JSON_ESCAPING_CHARACTERS_REPLACEMENT = "\\\\$1";

    private StringUtilities() {
    }

    /**
     * Returns an enumeration string for the given number. E.g. `5` return `5th`.
     *
     * @param number the number to convert
     *
     * @return a String representing the ordinal number
     */
    public static String integerToOrdinal(int number) {
        return integerToOrdinal(number, false);
    }

    /**
     * Returns an enumeration string for the given number. E.g. `5` return `5th`. If {@code startCountingWithZero} is true `0` is translated to `1st` and `9`
     * to `8th`.
     * If the number is not positive and startCountingWithZero is false or number is negative and startCountingWithZero is true the method throws an
     * {@link IllegalArgumentException}.
     *
     * @param number                the number to convert
     * @param startCountingWithZero true to compensate counting from zero
     *
     * @return a String representing the ordinal number
     *
     * @throws IllegalArgumentException if the number implies a non-positive value
     */
    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
    @NotNull
    public static String integerToOrdinal(int number, boolean startCountingWithZero) throws IllegalArgumentException {
        if (startCountingWithZero) {
            number += 1;
        }
        if (number < 1) {
            throw new IllegalArgumentException(
                    "Number must be positive (or 0 if startCountingWithZero is true) but was "
                            + (number - (startCountingWithZero ? 1 : 0))
                            + '/' + startCountingWithZero
            );
        }
        switch (number % 100) {
            case 11:
            case 12:
            case 13:
                return number + "th";
            default:
                switch (number % 10) {
                    case 1:
                        return number + "st";
                    case 2:
                        return number + "nd";
                    case 3:
                        return number + "rd";
                    default:
                        return number + "th";
                }
        }
    }

    /**
     * Returns the result of {@code o.toString()} iff o is not null, {@code null} otherwise. This avoids creating {@code "null"} literals (which
     * {@link String#valueOf(Object)} does).
     *
     * @param o object to create a {@link String} from
     *
     * @return {@link String} representation or null
     */
    public static @Nullable String toStringIfExists(@Nullable Object o) {
        return o != null ? o.toString() : null;
    }

    private static String indentLinesBy(String text, int count, String indentationString) {
        if (text == null) {
            return null;
        }

        StringBuilder indentationBuilder = new StringBuilder("\n");
        for (int i = 0; i < count; i++) {
            indentationBuilder.append(indentationString);
        }
        indentationString = indentationBuilder.toString();

        Matcher matcher = LINEBREAK_PATTERN.matcher(text);
        return matcher.replaceAll(indentationString);
    }

    @Deprecated
    public static class PropertyValuePairStringBuilder {
        private final String objectEnd;
        private final String indentation;
        private final String pairSeparator;
        private final StringBuilder builder = new StringBuilder();

        public PropertyValuePairStringBuilder() {
            this(SCHEMA_START, SCHEMA_END, INDENTATION, PROPERTY_SEPARATOR);
        }

        public PropertyValuePairStringBuilder(String objectStart, String objectEnt, String indentation, String pairSeparator) {
            this.objectEnd = objectEnt;
            this.indentation = indentation;
            this.pairSeparator = pairSeparator;
            builder.append(objectStart);
        }

        public void addPair(String key, Object value) {
            builder.append(indentation)
                    .append(key)
                    .append(pairSeparator)
                    .append(StringUtilities.indentLinesBy(
                            value != null ? value.toString() : null,
                            1,
                            indentation
                    ))
                    .append(PROPERTY_SEPARATOR);
        }

        public void addPair(Pair<String, ?> pair) {
            this.addPair(pair.getLeft(), pair.getRight());
        }

        public String toString() {
            String result = builder.replace(builder.length() - PROPERTY_SEPARATOR.length(), builder.length(), objectEnd).toString();

            //revert change
            builder.replace(builder.length() - objectEnd.length(), builder.length(), pairSeparator);

            return result;
        }
    }

    public static String listToJson(@Nullable List<?> list) {
        if (list == null) {
            return null;
        }
        return streamToJson(list.stream());
    }

    public static String arrayToJson(@Nullable Object[] array) {
        if (array == null) {
            return null;
        }
        return streamToJson(Arrays.stream(array));
    }

    public static String mapToJson(@Nullable Map<?, ?> map) {
        if (map == null) {
            return null;
        }
        return map.entrySet().stream()
                .map(entry -> new Pair<>(
                        JSON_KEY_QUOTE + stringToJsonObjectKey(Objects.toString(entry.getKey())) + JSON_KEY_QUOTE,
                        Objects.toString(entry.getValue())
                )).map(pair -> {
                    String s = pair.getRight();
                    if (!(s.startsWith(JSON_LIST_START) || s.startsWith(JSON_OBJECT_START) || s.startsWith(JSON_KEY_QUOTE) && s.endsWith(JSON_KEY_QUOTE))) {
                        s = JSON_KEY_QUOTE + s + JSON_KEY_QUOTE;
                    }
                    return pair.getLeft() + " : " + s;
                }).collect(Collectors.joining(JSON_ELEMENT_DELIMITER, JSON_OBJECT_START, JSON_OBJECT_END));
    }

    public static String setToJson(@Nullable Set<?> set) {
        if (set == null) {
            return null;
        }
        return streamToJson(set.stream());
    }

    public static @NotNull String stringToJsonObjectKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Json does not accept null as a key in an object (c.f. www.json.org).");
        }
        return JSON_ESCAPING_CHARACTERS_PATTERN.matcher(key).replaceAll(JSON_ESCAPING_CHARACTERS_REPLACEMENT);
    }

    private static String streamToJson(@NotNull Stream<?> stream) {
        return stream
                .map(Objects::toString)
                .map(s -> {
                    if (s.startsWith(JSON_LIST_START) || s.startsWith(JSON_OBJECT_START) || s.startsWith(JSON_KEY_QUOTE) && s.endsWith(JSON_KEY_QUOTE)) {
                        return s;
                    } else {
                        return JSON_KEY_QUOTE + s + JSON_KEY_QUOTE;
                    }
                }).collect(Collectors.joining(JSON_ELEMENT_DELIMITER, JSON_LIST_START, JSON_LIST_END));
    }
}
