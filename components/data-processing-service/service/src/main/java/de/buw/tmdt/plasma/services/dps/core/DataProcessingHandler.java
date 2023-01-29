package de.buw.tmdt.plasma.services.dps.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.buw.tmdt.plasma.datamodel.CombinedModel;
import de.buw.tmdt.plasma.services.dps.conversion.ConversionException;
import de.buw.tmdt.plasma.services.dps.conversion.rdf.RDFConverter;
import de.buw.tmdt.plasma.services.kgs.shared.feignclient.OntologyApiClient;
import feign.FeignException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DataProcessingHandler {

    private final OntologyApiClient ontologyApiClient;
    private final DataStorageHandler dataStorageHandler;

    private static final Logger log = LoggerFactory.getLogger(DataProcessingHandler.class);

    @Autowired
    public DataProcessingHandler(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
                                             OntologyApiClient ontologyApiClient,
                                 DataStorageHandler dataStorageHandler) {
        this.ontologyApiClient = ontologyApiClient;
        this.dataStorageHandler = dataStorageHandler;
    }

    public String processFile(CombinedModel template, String dataId, String fileId) {
        List<File> files = dataStorageHandler.listFiles(dataId);
        File file;
        if (fileId == null) {
            file = files.get(files.size() - 1);
        } else {
            file = files.stream()
                    .filter(x -> fileId.equals(x.getName()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requested fileId could not be found"));
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = null;
        try {
            root = mapper.readTree(file);
        } catch (IOException e) {
            log.info("Could not read contents of file {}", file.getName(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to read file.");
        }
        List<JsonNode> nodes = new ArrayList<>();
        if (root.isArray()) {
            root.elements().forEachRemaining(nodes::add);
        } else {
            nodes.add(root);
        }

        try {
            Map<String, String> prefixes = ontologyApiClient.getNamespaces();
            PrefixMapping prefixMapping = new PrefixMappingImpl();
            prefixMapping.setNsPrefixes(prefixes);
            RDFConverter converter = new RDFConverter(prefixMapping);
            Model combined = ModelFactory.createDefaultModel();
            combined.setNsPrefixes(prefixes);
            template.getSemanticModel().setId(UUID.randomUUID().toString());
            for (JsonNode node : nodes) {
                Model model = converter.convertToRDF(template, node);
                combined.add(model);
            }
            return asTurtle(combined);
        } catch (ConversionException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not convert file content(s): " + e.getMessage());
        } catch (FeignException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not obtain prefixes from KGS.");
        }
    }

    public static String asTurtle(Model model) {
        StringWriter out = new StringWriter();
        model.write(out, "TURTLE");
        return out.toString();
    }

}
