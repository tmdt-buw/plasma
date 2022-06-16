package de.buw.tmdt.plasma.services.kgs.shared.model;

import java.util.Arrays;
import java.util.Optional;

public enum ExportFormat {

    RDFXML("RDF/XML"),
    RDFXMLABBRV("RDF/XML-ABBREV"),
    NTRIPLE("N-TRIPLE"),
    TURTLE("TURTLE"),
    N3("N3");

    private final String format;

    ExportFormat(String formatString) {
        this.format = formatString;
    }

    public String getFormatString() {
        return format;
    }

    public static Optional<ExportFormat> get(String formatString) {
        return Arrays.stream(ExportFormat.values())
                .filter(format -> format.getFormatString().equalsIgnoreCase(formatString))
                .findFirst();
    }
}
