import logging
import pickle
from typing import Tuple

import networkx as nx
from gensim.models import Word2Vec

import os
import config
import json
from config import Environment as env
from modelextension.statistics_recommender import StatisticsRecommender
import modelextension.n2v as n2v


node2vec_model: Word2Vec
statistic_recommender: StatisticsRecommender



def init():
    global node2vec_model
    global statistic_recommender
    triples = []
    with open(config.CACHE.ENCODED_TRIPLES_PATH, 'r') as file:
        triples = pickle.loads(json.load(file).encode('latin-1'))

    if not os.path.isfile(config.CACHE.WORD2VEC_MODEL_PATH):
        # initialize node2vec embeddings
        n2v.init(triples)
        print("rebuild node2vec embeddings")
    node2vec_model = Word2Vec.load(config.CACHE.WORD2VEC_MODEL_PATH)

    if not os.path.isfile(config.CACHE.SR_MODEL_PATH):
        # initialize statistics recommender
        statistic_recommender = StatisticsRecommender(triples)
        statistic_recommender.store(config.CACHE.SR_MODEL_PATH)
        print("rebuild statistics recommender for ME")
    else:
        statistic_recommender = StatisticsRecommender.load(config.CACHE.SR_MODEL_PATH)


def recommend(positives: [str], limit: int = 5) -> [(str, float)]:

    positives = list(set(positives))  # remove duplicates

    try:
        n2v_most_similar = node2vec_model.wv.most_similar(
            topn=limit,
            positive=positives)
    except:
        logging.warning(f"Failed to retrieve n2v most similar for positives: {positives}")
        n2v_most_similar = []
        return n2v_most_similar

    # get most similar target nodes based on the result from node2vec
    most_similar_target_nodes:[(int, float)] = []

    logging.debug("n2v most similar")
    for node, probability in n2v_most_similar:
        logging.debug(f"\t{node} {probability}")
        most_similar_target_nodes = [(int(similar[0]),similar[1]) for similar in n2v_most_similar]

    logging.debug(f'most similar target nodes: {most_similar_target_nodes}')

    return most_similar_target_nodes


def recommend_focus(history: [str], limit: int = 5) -> [str]:
    most_similar_target_nodes = recommend(history, limit)

    return [x[0] for x in most_similar_target_nodes]  # drop the probabilities (for now)


def recommend_general(model: nx.Graph, limit: int = 5) -> [str]:
    aggregate = {}
    candidates = []
    for node in model.nodes:
        neighborhood = [str(x) for x in model.neighbors(node)]  # 1 hop neighbors
        filter_nodes = model.nodes
        most_similar_target_nodes = recommend([str(node)] + neighborhood, limit)
        most_similar_target_nodes = [uri for uri in most_similar_target_nodes if uri[0] not in filter_nodes]
        # most_similar_target_nodes = most_similar_target_nodes[:1]
        logging.debug(f"{node} -> {most_similar_target_nodes}")
        #for uri, certainty in most_similar_target_nodes:
        #    if uri not in aggregate:
        #        aggregate[uri] = 0
        #    aggregate[uri] += certainty
        candidates.append({
            'node': node,
            'mstn': most_similar_target_nodes,
            'cert': sum([c for n,c in most_similar_target_nodes]) / len(most_similar_target_nodes) if len(most_similar_target_nodes) > 0 else 1

        })

    candidates = sorted(candidates, key=lambda x:x['cert'], reverse=True)
    candidates_max = {}
    for candidate in candidates:
        for uri, certainty in candidate['mstn']:
            if not uri in candidates_max:
                candidates_max[uri] = certainty
            else:
                candidates_max[uri] = max(candidates_max[uri], certainty)
            if uri not in aggregate:
                aggregate[uri] = 0
            aggregate[uri] += certainty

    aggregate = sorted(aggregate.items(), key=lambda x: x[1], reverse=True)
    aggregate = sorted(candidates_max.items(), key=lambda x: x[1], reverse=True)

    return aggregate[:limit]
