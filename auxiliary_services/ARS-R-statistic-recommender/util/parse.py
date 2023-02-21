import os

import numpy as np
import rdflib
from rdflib import Namespace, Graph


def generate_id_dict(global_data):
    """Creates a dictionary mapping global_data's entries to unique integers."""
    d = {value: i for i, value in enumerate(set(global_data))}
    return d


def get_graphs_from_rdf(directory) -> [rdflib.Graph]:
    """Extracts graphs, subjects and objects from rdf files in given directory.

    Arguments:
      directory: The parent directory to the rdf files.

    Returns:
      graph_list: List of graphs created from rdf files
    """

    # list of rdf files.
    rdf_paths = os.listdir(directory)
    # List that will collect all the graphs
    graph_list = []

    for rdf_path in rdf_paths:
        with open(directory + "/" + rdf_path, encoding='utf-8') as rdf_file:
            # create a Graph with file name as identifier for debugging purposes...
            g = rdflib.Graph(identifier=rdf_path[:-4])
            # ... with the content of rdf_file
            g.parse(rdf_file)

            graph_list.append(g)

    return graph_list


def get_graphs_from_ttl(directory) -> [rdflib.Graph]:
    """Extracts graphs, subjects and objects from rdf files in given directory.

    Arguments:
      directory: The parent directory to the rdf files.

    Returns:
      graph_list: List of graphs created from rdf files
    """

    # list of rdf files.
    rdf_paths = os.listdir(directory)
    # List that will collect all the graphs
    graph_list = []

    for rdf_path in rdf_paths:
        with open(directory + "/" + rdf_path) as rdf_file:
            # create a Graph with file name as identifier for debugging purposes...
            g = rdflib.Graph(identifier=rdf_path[:-4])
            g.namespace_manager.bind("plasma", Namespace("http://tmdtkg#"))
            # ... with the content of rdf_file
            g.parse(rdf_file, format='turtle')

            graph_list.append(g)

    return graph_list


def get_classes_and_predicates(triples: [(str, str, str)]):
    # collect predicates and types
    all_preds = set([a for (s, a, t) in triples])
    all_classes = set([s for (s, a, t) in triples]).union([t for (s, a, t) in triples])

    return all_classes, all_preds


def encode_triples(triples: [(str, str, str)], node_dict, pred_dict):
    return np.array([np.array([node_dict[s], pred_dict[a], node_dict[t]], dtype=np.int32) for (s, a, t) in triples],
                    dtype=np.int32)


def find_class(classes_mapping, i):
    for clazz, index in classes_mapping.items():
        if index == i:
            return clazz


def find_relation(predicates_mapping, i):
    for pred, index in predicates_mapping.items():
        if index == i:
            return pred


