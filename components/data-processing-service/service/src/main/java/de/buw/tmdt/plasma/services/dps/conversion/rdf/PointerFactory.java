package de.buw.tmdt.plasma.services.dps.conversion.rdf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static de.buw.tmdt.plasma.datamodel.syntaxmodel.SchemaNode.ARRAY_PATH_TOKEN;

public class PointerFactory {

    private static final Logger log = LoggerFactory.getLogger(PointerFactory.class);
    private static final String TOKEN_START = "{#";
    private static final String TOKEN_END = "#}";
    private ArrayList<Integer> levelIndexes = new ArrayList<>();
    private ArrayList<String> nodePath;
    private String originalPath;

    public PointerFactory(ArrayList<String> nodePath) {
        int index = 0;
        // replace the array indicators with placeholders
        while (nodePath.contains(ARRAY_PATH_TOKEN)) {
            int arrayIndex = nodePath.indexOf(ARRAY_PATH_TOKEN);
            if (arrayIndex < 0) {
                break;
            }
            nodePath.set(arrayIndex, TOKEN_START + index++ + TOKEN_END);
            levelIndexes.add(0);
        }
        this.nodePath = nodePath;
        this.originalPath = composePath();
    }

    public String composePath() {
        return String.join("/", composeTokenizedPath());
    }

    public int getDepth() {
        return levelIndexes.size() - 1;
    }

    public String composePath(int level) {
        String token = TOKEN_START + level + TOKEN_END;
        int index = nodePath.indexOf(token);
        if (index < 0) {
            log.error("FAIL");
        }
        ArrayList<String> tokenizedPath = composeTokenizedPath();
        List<String> subpath = new ArrayList<>(tokenizedPath.subList(0, index));
        return String.join("/", subpath);
    }

    private ArrayList<String> composeTokenizedPath() {
        ArrayList<String> composed = new ArrayList<>(nodePath);
        for (int i = 0; i < levelIndexes.size(); i++) {
            String token = TOKEN_START + i + TOKEN_END;
            int index = composed.indexOf(token);
            if (index < 0) {
                log.error("FAIL");
            }
            composed.set(index, String.valueOf(levelIndexes.get(i)));
        }
        return composed;
    }

    public void increaseIndex(int level) {
        levelIndexes.set(level, levelIndexes.get(level) + 1);
        for (int i = level + 1; i < levelIndexes.size(); i++) {
            levelIndexes.set(i, 0);
        }
    }


    public void peekPath(int level, int index) {

    }

    @Override
    public String toString() {
        return composePath();
    }


    public String getOriginalPath() {
        return originalPath;
    }
}
