import json
import os
from pathlib import Path
from typing import Tuple

import config
from modelbuilder import modelbuilder
from modelextension import model_extender as me
from linkprediction import link_predictor as lp

classes_mapping: {}
predicates_mapping: {}


def decode(triple: Tuple[int, int, int]) -> Tuple[str, str, str]:
    return (class_uri(triple[0]), predicate_uri(triple[1]), class_uri(triple[2]))


def encode(triple: Tuple[str, str, str]) -> Tuple[int, int, int]:
    return (class_id(triple[0]), predicate_id(triple[1]), class_id(triple[2]))


def predicate_uri(pred_id: int) -> str:
    for pred, i in predicates_mapping.items():
        if pred_id == i:
            return pred


def predicate_id(pred_name: str) -> int:
    if pred_name not in predicates_mapping:
        return -1
    return predicates_mapping[pred_name]


def class_uri(class_id: int) -> str:
    for clazz, i in classes_mapping.items():
        if class_id == i:
            return clazz


def class_id(class_name: str) -> int:
    if class_name not in classes_mapping:
        return -1
    return classes_mapping[class_name]


class LinkedModelExtension:
    triple: Tuple[str]
    confidence: float


def init():
    global classes_mapping
    global predicates_mapping
    if not os.path.isfile(config.CACHE.CLASSES_MAPPING_PATH) or \
            not os.path.isfile(config.CACHE.PREDICATES_MAPPING_PATH) or \
            not os.path.isfile(config.CACHE.SR_MODEL_PATH) or \
            not os.path.isfile(config.CACHE.WORD2VEC_MODEL_PATH) or \
            not os.path.isfile(config.CACHE.RAW_MODELS_PATH):
        clear_cache()
        rebuild_cache()
    else:
        with open(config.CACHE.CLASSES_MAPPING_PATH, "r") as file:
            classes_mapping = json.load(file)

        with open(config.CACHE.PREDICATES_MAPPING_PATH, "r") as file:
            predicates_mapping = json.load(file)
        me.init()
        lp.init()


def clear_cache():
    if not os.path.isdir(config.CACHE.BASE_PATH):
        os.makedirs(config.CACHE.BASE_PATH, exist_ok=True)
    for file in os.listdir(config.CACHE.BASE_PATH):
        os.remove(Path(config.CACHE.BASE_PATH, file))


def rebuild_cache():
    global classes_mapping
    global predicates_mapping
    print("rebuilding cache")
    # rebuild model index
    modelbuilder.get_models()
    assert os.path.isfile(config.CACHE.RAW_MODELS_PATH)
    # generate mappings (number coding is used for each class and property)
    classes_mapping, predicates_mapping = modelbuilder.generate_mappings()
    assert os.path.isfile(config.CACHE.CLASSES_MAPPING_PATH)
    assert os.path.isfile(config.CACHE.PREDICATES_MAPPING_PATH)
    assert os.path.isfile(config.CACHE.ENCODED_TRIPLES_PATH)

    # initialize model extender
    me.init()

    # initialize link predictor
    lp.init()

    print("rebuilding cache done")


def generate_lme(
        anchor: str,
        context=None,
        filter_invalid: bool = False,
        limit: int = 3):
    if context is None:
        context = []

    # 0 encode URI to id for anchor and candidates (context)
    anchor_id = class_id(anchor)
    if anchor_id == -1:
        # unknown anchor
        return []
    context = [class_id(c) for c in context]
    # 1. candidate nodes
    # 1.1 specify context
    context.append(anchor_id)  # ensure that the anchor node is always in context
    # 1.2 perform ME
    candidate_nodes = me.recommend(positives=context, limit=10)
    # 1.3 (optional) filter undesired results (increases performance)
    candidate_nodes = [(id, cert) for (id, cert) in candidate_nodes if cert > .4]
    # 2. link prediction
    # 2.1 perform LP for all combinations of (anchor, candidate node)
    lmes = lp.recommend(anchor_id, candidate_nodes, filter_invalid)
    # 2.2 decode result from ids to URIs
    lmes = [(decode(lme[0]), lme[1]) for lme in lmes]
    # 3 preprocessing
    # 3.1 limit to top n results
    return lmes[:limit]
