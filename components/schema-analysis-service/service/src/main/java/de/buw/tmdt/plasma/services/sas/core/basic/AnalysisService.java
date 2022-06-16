package de.buw.tmdt.plasma.services.sas.core.basic;

import de.buw.tmdt.plasma.services.sas.core.model.Analysis;
import de.buw.tmdt.plasma.services.sas.core.model.syntaxmodel.Node;
import de.buw.tmdt.plasma.services.sas.core.repository.AnalysisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityManager;
import java.util.UUID;

@Service
public class AnalysisService {

    private final AnalysisRepository analysisRepository;
    private final EntityManager entityManager;

    @Autowired
    public AnalysisService(AnalysisRepository analysisRepository, EntityManager entityManager) {
        this.analysisRepository = analysisRepository;
        this.entityManager = entityManager;
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
        SchemaAnalysisAggregateRecognizedSchema schemaAnalysisAggregateRecognizedSchema = new SchemaAnalysisAggregateRecognizedSchema(20);
        for (Node n : analysis.getSubAnalyses()) {
            schemaAnalysisAggregateRecognizedSchema.addNode(n);
        }

        UUID newUuid = analysis.getUuid();

        Analysis finalAnalysis = new Analysis(newUuid);

        // Deleting the old analysis as only the result is needed. Otherwise there will be id conflicts
        analysisRepository.delete(analysis);
        analysisRepository.flush();

        finalAnalysis.setResult(schemaAnalysisAggregateRecognizedSchema.getMergedRecognizedNode());

        analysisRepository.save(finalAnalysis);
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