import os
from pathlib import Path

import networkx as nx
from node2vec import Node2Vec

import config as config
from config import Environment as env

os.environ["KMP_DUPLICATE_LIB_OK"] = "TRUE"


def init(triples: [(str, str, str)]):
    # if env.NODE2VEC_SELECT == 0:  # ontology based
    # node2vec_ontology()
    if env.NODE2VEC_SELECT == 1:  # model based
        node2vec_models(triples)


def node2vec_models(triples: [(str, str, str)]):
    sums = {}
    weights = {}
    outgoing = {}

    for (s, a, t) in triples:
        if not (s, a, t) in weights:
            weights[(s, a, t)] = 0
        weights[(s, a, t)] += 1
        if not (s, t) in sums:
            sums[(s, t)] = 0
        if not s in outgoing:
            outgoing[s] = 0
        outgoing[s] += 1
        sums[(s, t)] += 1

    # weights = dict(sorted(weights.items(), key=lambda item: item[1], reverse=True))
    sorted_sums = sorted(sums.items(), reverse=True, key=lambda x: x[1])
    # print(sorted_sums)

    total = sum([count for count in sums.values()])

    graph = nx.Graph()
    for (s, t), count in sums.items():
        weight = sums[(s, t)] / outgoing[s]
        graph.add_edge(str(s), str(t), weight=weight)

    model_based_node2vec = Node2Vec(graph,
                                    p=config.NodeEmbeddings.p,
                                    q=config.NodeEmbeddings.q,
                                    dimensions=config.NodeEmbeddings.dimension,
                                    walk_length=config.NodeEmbeddings.walk_length,
                                    num_walks=config.NodeEmbeddings.walks,
                                    workers=config.NodeEmbeddings.workers)
    print("walks size", len(model_based_node2vec.walks), model_based_node2vec.num_walks)

    model_based_model = model_based_node2vec.fit(window=config.NodeEmbeddings.window, sg=1,
                                                 negative=config.NodeEmbeddings.negative,
                                                 ns_exponent=config.NodeEmbeddings.ns_exponent)
    model_based_model.save(config.CACHE.WORD2VEC_MODEL_PATH)
