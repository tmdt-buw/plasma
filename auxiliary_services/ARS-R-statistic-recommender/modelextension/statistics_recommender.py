import json
import pickle
from collections import Counter
from pathlib import Path
from typing import Dict, List, Tuple

import config
from config import Environment as env

import numpy as np


class StatisticsRecommender:
    triples: [(str, str, str)]
    matrix: np.array
    pred_count: int

    def __init__(self, triples:List[Tuple[int,int,int]]=None):
        if triples is None:
            triples = [[]]
        self.triples = triples
        self.matrix = np.array(triples, dtype='float64')
        if self.matrix.size == 0:
            self.pred_count = 0
        else:
            self.pred_count = int(np.max(self.matrix[:, 1]) + 1)  # get the length of the return array based on number of distinct predicates

    def most_similar_classes(self, anchors: [str], topn: int = 5) -> [(str, float)]:
        # for each of the positives, generate a list of most likely values
        neighbors = [self.__get_neighbors(term) for term in anchors]
        # merge the lists
        neighbors = [x for xs in neighbors for x in xs]
        # count terms
        counter = Counter(neighbors)
        total = sum(list(counter.values())[:topn])
        matches = sorted(list(counter.items()), key=lambda x: x[1], reverse=True)
        matches = [(concept, count / total) for concept, count in matches]

        return matches[:topn]

    def __get_neighbors(self, anchor: str):
        return list(set([t for (s, a, t) in self.triples if s == anchor]))

    def predict_link(self, anchor: int, candidate: int):
        array = self.matrix

        filtered = array[np.logical_and(array[:, 0] == anchor, array[:, 2] == candidate), :]
        predicates = filtered[:, 1]
        if filtered.size == 0:
            return np.zeros([1, int(self.pred_count)], dtype='float64')
        pred_count = np.unique(predicates, return_counts=True)
        pred_count = np.transpose(pred_count)
        pred_count[:, 1] = pred_count[:, 1] / len(filtered)
        pred_count = pred_count[pred_count[:, 1].argsort()]

        result = np.zeros([1, int(self.pred_count)], dtype='float64')
        for i in range(len(pred_count)):
            result[0, int(pred_count[i][0])] = pred_count[i][1]
        return result

    def predict_links(self, tuples: [(int, int)]):
        result = np.empty([0, self.pred_count])
        for tup in tuples:
            row = self.predict_link(anchor=tup[0], candidate=tup[1])
            result = np.vstack((result, row))
        if result is None:
            result = np.empty([0, 0])
        return result

    def store(self, path: str = config.CACHE.SR_MODEL_PATH):
        with open(path, "w") as file:
            json.dump(pickle.dumps(self.matrix).decode('latin-1'), file)

    @staticmethod
    def load(path: str = config.CACHE.SR_MODEL_PATH):
        sr = StatisticsRecommender()
        with open(path, "r") as file:
            sr.matrix = pickle.loads(json.load(file).encode('latin-1'))
        sr.pred_count = int(np.max(sr.matrix[:, 1]) + 1)
        return sr
