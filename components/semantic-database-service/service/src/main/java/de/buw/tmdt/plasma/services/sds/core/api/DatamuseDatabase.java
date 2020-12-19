package de.buw.tmdt.plasma.services.sds.core.api;

import de.buw.tmdt.plasma.services.sds.shared.dto.concept.SDConceptDTO;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import de.buw.tmdt.plasma.services.sds.shared.dto.Database;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Collections.emptySet;

@Service("DatamuseDatabase")
@SuppressFBWarnings(value = "URLCONNECTION_SSRF_FD", justification = "public url that is predefined and the method only an abstraction")
public class DatamuseDatabase implements SemanticDatabase {

    private static final Logger logger = LoggerFactory.getLogger(DatamuseDatabase.class);
    private static final Pattern PATTERN = Pattern.compile("[ _]");

    @NotNull
    @Override
    public List<SDConceptDTO> findSynonyms(@NotNull String word) {
        String queryWord = PATTERN.matcher(word).replaceAll("+");
        return queryJSON("https://api.datamuse.com/words?ml=" + queryWord);
    }

    @NotNull
    @Override
    public List<SDConceptDTO> findHyponyms(@NotNull String word) {
        String queryWord = PATTERN.matcher(word).replaceAll("+");
        return queryJSON("https://api.datamuse.com/words?rel_gen=" + queryWord);
    }

    @NotNull
    @Override
    public List<SDConceptDTO> findHypernyms(@NotNull String word) {
        String queryWord = PATTERN.matcher(word).replaceAll("+");
        return queryJSON("https://api.datamuse.com/words?rel_spc=" + queryWord);
    }

    private List<SDConceptDTO> queryJSON(String url) {
        URLConnection connection;
        try {
            connection = new URL(url).openConnection();
        } catch (MalformedURLException e) {
            logger.warn("Invalid URL for DataMuse.", e);
            return Collections.emptyList();
        } catch (IOException e) {
            logger.warn("Failed to open connection to DataMuse", e);
            return Collections.emptyList();
        }

        StringBuilder stringBuilder;

        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String inputLine;
            stringBuilder = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                stringBuilder.append(inputLine);
            }
        } catch (IOException e) {
            logger.warn("Connection terminated while reading result", e);
            return Collections.emptyList();
        }

        return parseDataMuseString(stringBuilder.toString());
    }

    private List<SDConceptDTO> parseDataMuseString(String jsonIn) {
        JSONArray jsonArray = new JSONArray(jsonIn);
        int arraySize = jsonArray.length();

        List<SDConceptDTO> result = new ArrayList<>();
        for (int i = 0; i < arraySize; i++) {
            JSONObject entry = jsonArray.getJSONObject(i);
            String label = entry.getString("word");
            int score = entry.getInt("score");
            SDConceptDTO temp = new SDConceptDTO(
                    label,
                    "",
                    emptySet(),
                    Database.DATAMUSE.name() + ":UNKNOWN_ID",
                    emptySet(),
                    score
            );
            result.add(temp);
        }
        return result;
    }
}