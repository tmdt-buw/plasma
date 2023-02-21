package de.buw.tmdt.plasma.services.kgs.core;

import org.apache.jena.arq.querybuilder.DescribeBuilder;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.arq.querybuilder.UpdateBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.update.Update;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static de.buw.tmdt.plasma.services.kgs.core.OntologyManagement.PREFIXES;

@Service
public class SemanticModelManagement {

    private RDFConnectionRemoteBuilder builder;

    public SemanticModelManagement(@Value("${plasma.kgs.semanticmodels.store.url}") String semanticModelServerAddress) {
        builder = RDFConnectionFuseki.create()
                .destination(semanticModelServerAddress);
    }

    public Model queryModel(String modelId) {
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            Query selector = new DescribeBuilder()
                    .addPrefixes(PREFIXES)
                    .addVar("?model")
                    .addVar("?node")
                    .addWhere("?model", PLCM.cmUuid, modelId)
                    .addWhere("?model", PLCM.hasNode, "?node")
                    .addWhere("?node", "?p", "?o")
                    .build();

            QueryExecution query = conn.query(selector);
            Model model1 = query.execDescribe();
            return model1;
        }
    }

    public void persistModel(Model model) {
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            UpdateBuilder updateBuilder = new UpdateBuilder()
                    .addPrefixes(PREFIXES)
                    .addInsert(model);
            Update insert = updateBuilder.build();

            conn.update(insert);
        }
    }


    public void deleteModel(String semanticModelUuid) {
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            Query selector = new DescribeBuilder()
                    .addPrefixes(PREFIXES)
                    .addVar("?model")
                    .addVar("?node")
                    .addWhere("?model", PLCM.label, semanticModelUuid)
                    .addWhere("?model", PLCM.hasNode, "?node")
                    .addWhere("?node", "?p", "?o")
                    .build();


            QueryExecution query = conn.query(selector);
            Model model1 = query.execDescribe();

            Update delete = new UpdateBuilder()
                    .addPrefixes(PrefixMapping.Standard)
                    .addDelete(model1)
                    .build();

            conn.update(delete);

        }
    }

    public List<String> findByElementLabel(String searchTerm) {
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            SelectBuilder selectBuilder = new SelectBuilder();
            Query selector = selectBuilder
                    .addPrefixes(PREFIXES)
                    .addVar("?id")
                    .addWhere("?model", PLCM.hasNode, "?node")
                    .addWhere("?model", PLCM.cmUuid, "?id")
                    .addFilter(selectBuilder.getExprFactory().regex("?o", searchTerm, "i"))
                    .build();


            QueryExecution query = conn.query(selector);

            ResultSet resultSet = query.execSelect();
            List<String> ids = new ArrayList<>();
            while (resultSet.hasNext()) {
                QuerySolution querySolution = resultSet.next();
                Literal id = querySolution.getLiteral("id");
                ids.add(id.getString());
            }
            return ids;
        }
    }

}
