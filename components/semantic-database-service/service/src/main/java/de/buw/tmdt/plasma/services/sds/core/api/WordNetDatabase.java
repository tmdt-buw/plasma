package de.buw.tmdt.plasma.services.sds.core.api;

import de.buw.tmdt.plasma.services.sds.shared.dto.concept.SDConceptDTO;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import de.buw.tmdt.plasma.services.sds.shared.dto.Database;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;

@Service("WordNetDatabase")
public class WordNetDatabase implements SemanticDatabase {

    private static final Logger logger = LoggerFactory.getLogger(WordNetDatabase.class);
    private static final File WORDNET_HOME = Path.of("files", "database", "wordnet", "wordnet_3.1", "data").toFile();

    @NotNull
    @Override
    public List<SDConceptDTO> findSynonyms(@NotNull String word) {
        return performQuery(word, null);
    }

    @NotNull
    @Override
    public List<SDConceptDTO> findHyponyms(@NotNull String word) {
        return performQuery(word, Pointer.HYPONYM);
    }

    @NotNull
    @Override
    public List<SDConceptDTO> findHypernyms(@NotNull String word) {
        return performQuery(word, Pointer.HYPERNYM);
    }

    private List<SDConceptDTO> performQuery(@NotNull final String word, @Nullable Pointer semanticRelationType) {
        List<SDConceptDTO> result = new ArrayList<>();

        IDictionary dict = new Dictionary(WORDNET_HOME);
        List<IWordID> wordIDS;
        try {
            dict.open();

            IIndexWord idxWord = dict.getIndexWord(word, POS.NOUN);
            if (idxWord == null) {
                dict.close();
                return Collections.emptyList();
            }
            wordIDS = idxWord.getWordIDs();
        } catch (IOException ignore) {
            logger.error("Couldn't connect with WordNet database.");
            return Collections.emptyList();
        } finally {
            dict.close();
        }

        wordIDS = wordIDS.stream()
                .filter(iWordID -> !Character.isUpperCase(iWordID.getLemma().charAt(0)))
                .collect(Collectors.toList());

        for (IWordID wordID : wordIDS) {
            ISynset wordSynset = getSynset(wordID.getSynsetID());
            if (wordSynset == null) {
                return Collections.emptyList();
            }

            if (semanticRelationType == null) {
                SDConceptDTO concept = convertSynsetToDTO(wordSynset, wordID.getLemma());
                // Add all direct parent concepts to the context of the concept
                for (ISynsetID synsetID : getMoreGeneralSynsets(wordSynset)) {
                    ISynset parentSynset = getSynset(synsetID);
                    if (parentSynset != null) {
                        concept.addRelatedConcept(convertSynsetToDTO(parentSynset, null));
                    }
                }
                result.add(concept);
            } else {
                // Add all direct parent concepts to the context of the concept
                for (ISynsetID synsetID : getMoreGeneralSynsets(wordSynset, semanticRelationType)) {
                    ISynset parentSynset = getSynset(synsetID);
                    if (parentSynset != null) {
                        result.add(convertSynsetToDTO(parentSynset, null));
                    }
                }
            }
        }
        return result;
    }

    @NotNull
    private SDConceptDTO convertSynsetToDTO(@NotNull ISynset synset, @Nullable String label) {
        return new SDConceptDTO(
                // WordNet starts counting words with 1
                label != null ? label : synset.getWord(1).getLemma(),
                synset.getGloss(),
                synset.getWords().stream().map(IWord::getLemma).collect(Collectors.toSet()),
                Database.WORDNET.name() + ":" + synset.getID().toString(),
                emptySet(),
                0
        );
    }

    @Nullable
    private ISynset getSynset(@NotNull ISynsetID synsetID) {
        // construct the dictionary object and open it
        IDictionary dict = new Dictionary(WORDNET_HOME);
        try {
            dict.open();
            return dict.getSynset(synsetID);
        } catch (IOException ignore) {
            logger.error("Couldn't connect with WordNet database.");
            return null;
        } finally {
            dict.close();
        }
    }

    @NotNull
    private Set<ISynsetID> getRelatedWords(@NotNull ISynset synset, IPointer... pointers) {
        if (pointers == null || pointers.length == 0) {
            return new HashSet<>(synset.getRelatedSynsets());
        }
        Set<ISynsetID> res = new HashSet<>();
        for (IPointer pointer : pointers) {
            res.addAll(synset.getRelatedSynsets(pointer));
        }
        return res;
    }

    @NotNull
    private Set<ISynsetID> getMoreGeneralSynsets(@NotNull ISynset synset) {
        Pointer[] moreGeneralPointers = {
                Pointer.HYPERNYM,
                Pointer.HOLONYM_MEMBER,
                Pointer.HOLONYM_PART,
                Pointer.HOLONYM_SUBSTANCE};
        return getRelatedWords(synset, moreGeneralPointers);
    }

    @NotNull
    private Set<ISynsetID> getMoreGeneralSynsets(@NotNull ISynset synset, @NotNull Pointer relationPointer) {
        if (!(relationPointer.equals(Pointer.HYPERNYM) || relationPointer.equals(Pointer.HYPONYM))) {
            return getMoreGeneralSynsets(synset);
        }
        return getRelatedWords(synset, relationPointer);
    }

}
