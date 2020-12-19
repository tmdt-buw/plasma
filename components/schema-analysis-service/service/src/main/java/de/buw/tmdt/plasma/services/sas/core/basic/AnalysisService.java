package de.buw.tmdt.plasma.services.sas.core.basic;

import de.buw.tmdt.plasma.services.sas.core.converter.RuleDTOConverter;
import de.buw.tmdt.plasma.services.sas.core.converter.SemanticModelDTOConverter;
import de.buw.tmdt.plasma.services.sas.core.model.Analysis;
import de.buw.tmdt.plasma.services.sas.core.model.syntaxmodel.Node;
import de.buw.tmdt.plasma.services.sas.core.repository.AnalysisRepository;
import de.buw.tmdt.plasma.services.sas.core.repository.StandardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class AnalysisService {

    private final AnalysisRepository analysisRepository;

    @Autowired
    public AnalysisService(AnalysisRepository analysisRepository,
                           SemanticModelDTOConverter semanticModelDTOConverter,
                           RuleDTOConverter ruleDTOConverter,
                           StandardRepository standardRepository) {
        this.analysisRepository = analysisRepository;
    }

    @Transactional
    public void createAnalysis(UUID uuid) {
        Analysis analysis = new Analysis(uuid);
        analysisRepository.save(analysis);
    }

    @Transactional
    public void addNodeToAnalysis(UUID uuid, Node node) {
        Analysis analysis =
                analysisRepository.findById(uuid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Analysis ID not found"));
        analysis.addSubAnalysis(node);
        analysisRepository.save(analysis);
    }

    @Transactional
    public void calculateResult(UUID uuid) {
        Analysis analysis = analysisRepository.findById(uuid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Analysis ID not found"));
        SchemaAnalysisAggregateRecognizedSchema schemaAnalysisAggregateRecognizedSchema = new SchemaAnalysisAggregateRecognizedSchema(1);
        for (Node n : analysis.getSubAnalyses()) {
            schemaAnalysisAggregateRecognizedSchema.addNode(n);
        }

        analysis.setResult(schemaAnalysisAggregateRecognizedSchema.getMergedRecognizedNode());
        analysisRepository.save(analysis);
    }


    @Transactional
    public Node getResult(UUID uuid) {
        Analysis analysis = analysisRepository.findById(uuid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Analysis ID not found"));
        return analysis.getResult();
    }

    public boolean hasResult(UUID uuid) {

        Analysis analysis = analysisRepository.findById(uuid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Analysis ID not found"));
        return analysis.getResult() != null;
    }

    public boolean exists(UUID uuid) {
        return analysisRepository.existsById(uuid);
    }

    @Transactional
    public void delete(UUID uuid) {
        analysisRepository.deleteById(uuid);
    }
}