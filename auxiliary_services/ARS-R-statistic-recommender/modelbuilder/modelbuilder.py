import json
import os
import pickle
from typing import Dict

from SPARQLWrapper import SPARQLWrapper, JSON, XML
from rdflib import Graph

import config
from util.graphutil import uninstanciate_graph, get_triples_from_model
from util.namespaces import PREFIXES, PLCM
from util.parse import get_classes_and_predicates, generate_id_dict, encode_triples


def get_models():
    # establish connection to triple store (currently Fuseki)
    host = os.getenv('SPARQL_SM_STORE')
    if not host:
        print("WARN", "No SPARQL endpoint defined to request semantic models. 'SPARQL_SM_STORE' is empty.")
        return

    sparql = SPARQLWrapper(host)
    sparql.setQuery(PREFIXES + """
        SELECT ?id WHERE {
          ?model plcm:cmUuid ?id .
        } LIMIT 100
    """)
    # sparql.setCredentials("admin", "plasma")
    sparql.setReturnFormat(JSON)
    results = sparql.query().convert()

    print("found models", len(results["results"]["bindings"]))

    models = []

    for result in results["results"]["bindings"]:
        model_id = result["id"]['value']
        print('processing', model_id)

        query = PREFIXES + f"""
                    DESCRIBE ?node WHERE {{
                      ?model plcm:cmUuid "{model_id}" .
                      ?model plcm:hasNode ?node .
                      ?node ?p ?o .
                    }}
                """

        sparql.setReturnFormat(XML)
        sparql.setQuery(query)

        model_graph: Graph = sparql.queryAndConvert()

        for triple in model_graph.triples((None, PLCM.cmUuid, None)):
            model_graph.remove(triple)

        for triple in model_graph.triples((None, PLCM.hasNode, None)):
            model_graph.remove(triple)

        triples = list(model_graph.triples((None, None, None)))
        print('model size', len(triples))

        model_graph = uninstanciate_graph(model_graph)

        triples = list(model_graph.triples((None, None, None)))
        print('uninstanciated model size', len(triples))

        triples = get_triples_from_model(model_graph)
        models.append(triples)

    with open(config.CACHE.RAW_MODELS_PATH, "w") as file:
        json.dump(models, file)


def build_label_index():
    host = os.getenv('SPARQL_ONTOLOGIES_STORE')
    if not host:
        print("WARN", "No SPARQL endpoint defined to request ontologies. 'SPARQL_ONTOLOGIES_STORE' is empty.")
        return

    sparql = SPARQLWrapper(host)
    sparql.setQuery(PREFIXES + """
            SELECT ?uri ?label ?comment WHERE {
                    ?uri rdfs:label ?label .
                    OPTIONAL { ?uri rdfs:comment ?comment }
        } 
        """)
    # FILTER(?uri in (<uri1>,<uri2>))

    # sparql.setCredentials("admin", "plasma")
    sparql.setReturnFormat(JSON)
    results = sparql.query().convert()

    meta_info: Dict[str, {str, str}] = {}

    for result in results["results"]["bindings"]:
        uri = result["uri"]['value']
        label = result["label"]['value']
        if "comment" in result:
            comment = result["comment"]['value']

        meta_info[uri] = {'label':label, 'comment':comment}

    return meta_info


def generate_mappings():
    with open(config.CACHE.RAW_MODELS_PATH, "r") as file:
        models = json.load(file)

    triples = []
    for model in models[:]:
        for triple in model:
            triples.append(tuple(triple))

    classes, predicates = get_classes_and_predicates(triples)
    classes_mapping = generate_id_dict(classes)
    predicates_mapping = generate_id_dict(predicates)
    with open(config.CACHE.CLASSES_MAPPING_PATH, "w") as file:
        json.dump(classes_mapping, file)

    with open(config.CACHE.PREDICATES_MAPPING_PATH, "w") as file:
        json.dump(predicates_mapping, file)

    encoded_triples = encode_triples(triples, classes_mapping, predicates_mapping)
    with open(config.CACHE.ENCODED_TRIPLES_PATH, "w") as file:
        json.dump(pickle.dumps(encoded_triples).decode('latin-1'), file)

    return classes_mapping, predicates_mapping
