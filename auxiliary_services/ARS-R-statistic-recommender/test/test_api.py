import unittest

import service
from schemas import CombinedModel, SemanticModel, SemanticModelNode


class TestStringMethods(unittest.TestCase):

    def test_generate_recommendation(self):
        service.init()
        cm: CombinedModel = CombinedModel()
        cm.recommendations = []
        cm.semanticModel = SemanticModel()
        smn = SemanticModelNode.parse_obj({"_class": "Class"})
        smn.uri = "http://plasma.uni-wuppertal.de/schema#latitude"
        cm.semanticModel.nodes = [smn]
        cm_with_recommendations: CombinedModel = service.generate_recommendations(combinedModel=cm)
        print("recommendations", cm_with_recommendations.recommendations)


if __name__ == '__main__':
    unittest.main()
