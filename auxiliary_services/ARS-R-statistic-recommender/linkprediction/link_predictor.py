import json
import os
import pickle
from operator import itemgetter
from typing import List, Tuple

import numpy as np

import config
from config import Environment as env
from modelextension.statistics_recommender import StatisticsRecommender

os.environ["KMP_DUPLICATE_LIB_OK"] = "TRUE"


class PredictionModels:
    model: any
    embed: any
    statistic_recommender: StatisticsRecommender


prediction_models: PredictionModels


def init():
    global prediction_models
    prediction_models = PredictionModels()

    if not os.path.isfile(config.CACHE.SR_MODEL_PATH):
        # initialize statistics recommender
        with open(config.CACHE.ENCODED_TRIPLES_PATH, "r") as file:
            triples = pickle.loads(json.load(file).encode('latin-1'))
        statistic_recommender = StatisticsRecommender(triples)
        statistic_recommender.store(config.CACHE.SR_MODEL_PATH)
        prediction_models.statistic_recommender = statistic_recommender
        print("rebuild statistics recommender for LP")
    else:
        prediction_models.statistic_recommender = StatisticsRecommender.load(config.CACHE.SR_MODEL_PATH)


def recommend(anchor: id,
              candidate_nodes: [(int, float)],
              filter_invalid: bool = False,
              limit: int = 10):
    if config.Environment.LP_SELECT == 0:
        return recommend_sr(anchor, candidate_nodes, filter_invalid, limit)


def recommend_sr(anchor: int,
                 candidate_nodes: [(int, float)],
                 filter_invalid: bool = False,
                 candidate_link_limit: int = 3,
                 limit: int = 10):
    sr = prediction_models.statistic_recommender
    # map the anchor to a mapped id
    tuples: [(int, int)] = [(anchor, candidate[0]) for candidate in candidate_nodes]  # TODO add inverse to support inverse relations

    link_predictions = sr.predict_links(tuples)

    result_list: List[Tuple[Tuple[int, int, int], float]] = []

    index = 0  # iterator to get the correct row from candidate tuples list
    for prediction_row in link_predictions:
        top_prediction_indices = np.argsort(prediction_row)[::-1]  # identify top indices in descending order
        for prediction_index in top_prediction_indices[:candidate_link_limit]:
            triple = (anchor, prediction_index, candidate_nodes[index][0])
            link_cert = prediction_row[prediction_index]
            if link_cert == 0:
                break  # no more valid link candidates
            node_cert = candidate_nodes[index][1]  # get the certainty of that candidate prediction (from ME phase)
            cert = .5 * link_cert + .5 * node_cert  # aggregate certainties
            result_list.append((triple, cert))
        index += 1

    top_list = sorted(result_list, key=itemgetter(1), reverse=True)  # sort by overall prediction certainty

    # if filter_invalid:
    #     for tuple in top_list:
    #         if tuple_in_ontology((tuple[0], tuple[1], tuple[2])):
    #             result_list.append(tuple)
    # else:
    # result_list = top_list

    if limit:
        top_list = top_list[:limit]

    if env.LOG_LEVEL > 20:
        print("results:", top_list)

    return top_list

# def recommend_rgcn(anchor: str,
#               candidate_nodes: [str],
#               filter_invalid: bool = False,
#               top_n: int = 10):
#     global concept_prediction_models
#     try:
#         prediction_models
#     except NameError:
#         init()
#
#     if not class_id(anchor, prediction_models.classes_mapping):
#         return []  # if the target node is not known, return empty results
#
#     candidate_relations = []
#     s_id = class_id(anchor, prediction_models.classes_mapping)
#     assert s_id is not None
#     for node in candidate_nodes:
#         o_id = class_id(node, prediction_models.classes_mapping)
#         assert o_id is not None
#         for pred, i in prediction_models.predicates_mapping.items():
#             embed = prediction_models.embed
#             w = prediction_models.model.w_relation
#             emb_triplet = embed[s_id] * w[i] * embed
#             scores = torch.sigmoid(torch.sum(emb_triplet, dim=1))
#             scores, indices = torch.sort(scores, descending=True)
#             rank = int((indices == o_id).nonzero())
#             score = scores[rank]
#
#             #subj = prediction_models.embed[s_id]
#             #rel = prediction_models.model.w_relation[i]
#             #obj = prediction_models.embed[o_id]
#             #score = torch.sum(subj * rel * obj)
#             candidate_relations.append(((str(anchor), str(pred), str(node)), score))
#
#     top_list = sorted(candidate_relations, key=lambda item: item[1], reverse=True)
#
#     result_list = []
#     if filter_invalid:
#         for tuple in top_list:
#             if tuple_in_ontology((tuple[0], tuple[1], tuple[2])):
#                 result_list.append(tuple)
#     else:
#         result_list = top_list
#
#     if top_n:
#         result_list = result_list[:top_n]
#
#     if env.LOG_LEVEL > 20:
#         print("results:", result_list)
#
#     return result_list
