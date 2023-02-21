import logging


class CACHE:
    BASE_PATH = "cache"
    RAW_MODELS_PATH = BASE_PATH + '/models.json'
    CLASSES_MAPPING_PATH = BASE_PATH + '/classes_mapping.json'
    PREDICATES_MAPPING_PATH = BASE_PATH + '/predicates_mapping.json'
    ENCODED_TRIPLES_PATH = BASE_PATH + "/encoded_triples.json"

    WORD2VEC_MODEL_PATH = BASE_PATH + '/word2vec.emb'
    SR_MODEL_PATH = BASE_PATH + '/statistics_recommender.json'


class Environment:

    # 5: TRACE -> All info
    # 10: DEBUG -> Some info
    # 20: INFO -> Basic info
    LOG_LEVEL = 20

    # 0 for working with ontology based node2vec, 1 for working with model based node2vec
    NODE2VEC_SELECT = 1

    # 0 for embedding based generation, 1 for statistic recommender
    ME_SELECT = 0

    # 0 for statistic recommender, 1 for RFC, 2 for RGCN
    LP_SELECT = 0


class NodeEmbeddings:
    p = 1.0
    q = 1.0
    workers = 4
    walk_length = 30
    walks = 1000
    window = 10
    dimension = 100
    negative = 5
    ns_exponent = 1.0


logging.addLevelName(5,"TRACE")
logging.basicConfig(level=Environment.LOG_LEVEL, format='%(asctime)s [%(levelname)s] - %(message)s')
