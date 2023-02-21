from rdflib import Graph, RDF, Literal, XSD


def get_triples_from_models(models) -> [(str, str, str)]:
    triples = []
    for model in models:
        triples += get_triples_from_model(model)
    return triples


def get_triples_from_model(model) -> [(str, str, str)]:
    triples = []
    if isinstance(model, Graph):
        for (s, p, o) in model.triples((None, None, None)):
            (s, p, o) = (str(s), str(p), str(o))
            triples.append((s, p, o))
    else:
        for triplet in model:
            triples.append((triplet[0],triplet[1],triplet[2]))

    # print(triples[:20])
    return triples


def uninstanciate_graph(graph: Graph) -> Graph:
    type_mapping = {}
    counter = 0
    for (sub, _, obj) in graph.triples((None, RDF.type, None)):
        type_mapping[sub] = obj

    uninstanciated_graph = Graph()
    for ns in graph.namespaces():
        uninstanciated_graph.namespace_manager.bind(ns[0], ns[1])

    for (sub, pred, obj) in graph.triples((None, None, None)):
        counter += 1

        if pred == RDF.type:
            continue

        if sub in type_mapping:
            sub = type_mapping[sub]
        else:
            continue

        if obj in type_mapping:
            obj = type_mapping[obj]
        elif isinstance(obj, Literal):
            obj = XSD.string

        uninstanciated_graph.add((sub, pred, obj))

    return uninstanciated_graph