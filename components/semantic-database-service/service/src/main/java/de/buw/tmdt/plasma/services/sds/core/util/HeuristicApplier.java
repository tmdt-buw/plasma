package de.buw.tmdt.plasma.services.sds.core.util;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
public class HeuristicApplier {

    /**
     * Applies all available heuristics to the given label in order to produce higher quality labels.
     *
     * @param label The labels, may contain problems like underscores, CamelCase, etc.
     * @return A set of high quality labels acquired from different heuristics.
     */
    @NotNull
    public String applyHeuristics(@NotNull String label) {
        String res = label;

        // Replace underscore with space
        res = underscoreRemoval(res);

        // Transform all values to lowercase
        res = toLowerCase(res);
        res = trimString(res);
        return res;
    }

    private String underscoreRemoval(String label) {
        return label.replace("_", " ");
    }

    private String trimString(String label) {
        return label.trim().replaceAll(" +", " ");
    }

    private String toLowerCase(String label) {
        return label.toLowerCase();
    }
}