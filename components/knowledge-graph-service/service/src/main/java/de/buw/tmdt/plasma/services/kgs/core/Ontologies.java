package de.buw.tmdt.plasma.services.kgs.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import de.buw.tmdt.plasma.services.kgs.shared.model.OntologyInfo;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.io.FilenameUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Component
@SuppressFBWarnings("PATH_TRAVERSAL_IN")
public class Ontologies {

    private static final Logger log = LoggerFactory.getLogger(Ontologies.class);

    static String SM_URI = "http://plasma.uni-wuppertal.de/sm/";

    private Map<Ontology, OntModel> ontologyModels = new HashMap<>();
    public static final PrefixMapping PREFIXES = new PrefixMappingImpl();
    @Value("${plasma.kgs.ontologies.metadata.suffix:.ontology}")
    private final String ontologySuffix = ".ontology";

    private final ObjectMapper mapper;
    @Value("${plasma.kgs.ontologies.folder:./ontologies}")
    private String ontologyFolder;
    private static final PrefixMapping DEFAULT_PREFIXES = new PrefixMappingImpl()
            .setNsPrefixes(PrefixMapping.Standard)
            .setNsPrefix("plcm", PLCM.getURI())
            .setNsPrefix("plsm", SM_URI);

    @Autowired
    public Ontologies() {
        mapper = new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .enable(SerializationFeature.INDENT_OUTPUT);
        DEFAULT_PREFIXES.getNsPrefixMap().forEach(PREFIXES::setNsPrefix);
    }

    @PostConstruct
    public void init() {
        reloadOntologies();
    }

    public synchronized void reloadOntologies() {
        Map<Ontology, OntModel> reloadedModels = new HashMap<>();
        Path ontologiesFolder = Paths.get(getOntologyFolder());
        if (!Files.exists(ontologiesFolder)) {
            if (!ontologiesFolder.toFile().mkdirs()) {
                log.warn("Could not create ontologies folder.");
            } else {
                log.info("Created ontologies folder on " + ontologiesFolder);
            }
        } else {
            log.info("Loading ontologies from " + ontologiesFolder);
        }
        try {
            Files.list(ontologiesFolder)
                    .forEach(filePath -> {
                        if (!("." + FilenameUtils.getExtension(filePath.toString())).equals(ontologySuffix)) {
                            return;
                        }
                        try {
                            Ontology ontology = mapper.readValue(filePath.toFile(), Ontology.class);
                            OntModel loadedOnto = ModelFactory.createOntologyModel();
                            try (FileInputStream fis = new FileInputStream(ontology.getFilePath())) {
                                loadedOnto.read(fis, "", "TURTLE"); // TODO handle different formats or homogenize
                            } catch (IOException fnfe) {
                                log.error("Could not read {}:", filePath, fnfe);
                                return;
                            }
                            reloadedModels.put(ontology, loadedOnto);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        ontologyModels = reloadedModels;
        PREFIXES.clearNsPrefixMap();

        PREFIXES.setNsPrefixes(DEFAULT_PREFIXES);
        ontologyModels.keySet().forEach(o -> PREFIXES.setNsPrefix(o.getPrefix(), o.getUri()));
    }

    public String getOntologyFolder() {
        return ontologyFolder;
    }

    public void addOntology(String label, String prefix, String namespace, MultipartFile ontologyFile) {
        String filename = ontologyFile.getOriginalFilename();

        if (filename == null || filename.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File must have a filename.");
        }
        if (Files.exists(Paths.get(getOntologyFolder(), filename))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This ontology already exists.");
        }
        label = label.replaceAll(" ", "_");

        try {
            // try loading the ontology
            OntModel loadedOnto = ModelFactory.createOntologyModel();

            try (InputStream is = ontologyFile.getInputStream()) {
                loadedOnto.read(is, "", "TURTLE"); // TODO handle different formats or homogenize
            } catch (IOException e) {
                log.error("Could not read received ontology {}:", ontologyFile.getOriginalFilename(), e);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not read the received ontology: " + e.getMessage());
            }

            Path metadataFilePath = Paths.get(getOntologyFolder(), label + ontologySuffix);
            if (Files.exists(metadataFilePath)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ontology for this label already exists");
            }

            log.info("Uploading ontology file");
            Path ontologyFilePath = Paths.get(getOntologyFolder(), filename);
            byte[] bytes = ontologyFile.getBytes();
            Files.write(Paths.get(getOntologyFolder(), filename), bytes);
            log.info("Written ontology file to " + ontologyFilePath);

            Ontology onto = new Ontology(label, ontologyFilePath.toString(), prefix, namespace);
            mapper.writeValue(metadataFilePath.toFile(), onto);
        } catch (IOException e) {
            log.error("Error while writing ontology or metadata file", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ontology could not be stored."
            );
        }
        // rebuild the index
        reloadOntologies();
    }

    public OntModel createLocalOntology(String label, String prefix, String namespace, String description) {
        label = label.replaceAll(" ", "_");
        String filename = label + ".ttl";

        if (Files.exists(Paths.get(getOntologyFolder(), filename))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This ontology already exists.");
        }
        OntModel createdOnto = ModelFactory.createOntologyModel();
        try {
            Path metadataFilePath = Paths.get(getOntologyFolder(), label + ontologySuffix);
            if (Files.exists(metadataFilePath)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ontology for this label already exists");
            }

            log.info("Creating local ontology file");
            Path ontologyFilePath = Paths.get(getOntologyFolder(), filename);
            try (OutputStream out = new FileOutputStream(ontologyFilePath.toFile())) {
                createdOnto.write(out, "TURTLE");
                log.info("Written ontology file to " + ontologyFilePath);
            }
            Ontology onto = new Ontology(label, ontologyFilePath.toString(), prefix, namespace, description, true);
            mapper.writeValue(metadataFilePath.toFile(), onto);
        } catch (IOException e) {
            log.error("Error while writing ontology or metadata file", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ontology could not be stored."
            );
        }
        // rebuild the index
        reloadOntologies();
        return createdOnto;
    }

    public void updateOntology(String label, Model model) {
        Optional<OntModel> ontologyModelByLabel = getOntologyModelByLabel(label);
        if (ontologyModelByLabel.isEmpty()) {
            return;
        }
        String filepath = ontologyModels.keySet().stream()
                .filter(ont -> ont.getLabel().equals(label))
                .map(Ontology::getFilePath)
                .findFirst()
                .orElseThrow();
        try {

            Path ontologyFilePath = Paths.get(filepath);
            try (OutputStream out = new FileOutputStream(ontologyFilePath.toFile())) {
                model.write(out, "TURTLE");
                log.info("Written updated model to " + ontologyFilePath);
            }

        } catch (IOException e) {
            log.error("Error while writing updated ontology", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ontology could not be stored."
            );
        }
        // rebuild the index
        reloadOntologies();
    }

    public List<OntologyInfo> listOntologies() {
        return ontologyModels.keySet().stream()
                .map(om -> new OntologyInfo(om.getLabel(), om.getPrefix(), om.getUri(), om.isLocal()))
                .collect(Collectors.toList());
    }

    public List<OntModel> getAllOntologyModels() {
        return new ArrayList<>(ontologyModels.values());
    }

    public List<OntModel> getFilteredOntologyModels(List<String> labels) {
        if (labels == null || labels.isEmpty()) {
            return new ArrayList<>(ontologyModels.values());
        }
        return ontologyModels.entrySet().stream()
                .filter(entry -> labels.contains(entry.getKey().getLabel()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    public Optional<OntModel> getOntologyModelByLabel(String label) {
        if (label == null || label.isBlank()) {
            return Optional.empty();
        }
        return ontologyModels.entrySet().stream()
                .filter(entry -> label.equals(entry.getKey().getLabel()))
                .map(Map.Entry::getValue)
                .findFirst();
    }
}