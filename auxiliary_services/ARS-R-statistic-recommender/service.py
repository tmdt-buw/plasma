import os
import uuid
from typing import Dict

import uvicorn
from fastapi import FastAPI
from rdflib import XSD

import modelbuilder.modelbuilder
from linkedmodelextension import linked_model_extender as lme
from schemas import CombinedModel, DeltaModification, SemanticModelNode, Literal, NamedEntity, Relation
from util.namespaces import PLSM, PLCM
import schedule

os.environ["KMP_DUPLICATE_LIB_OK"] = "TRUE"

app = FastAPI()

# cache to hold additional information for each URI: Format is uri -> {label, comment}
meta_info: Dict[str, Dict[str, str]] = {}

initialized: bool = False
resettime = "02:30"


# noinspection PyPep8Naming
# in order to match the API query parameter names used in Java components
@app.post("/recommendation")
async def recommend(combinedModel: CombinedModel, uuid: str = None, configId: str = None, configToken: str = None):
    global initialized
    if not initialized:
        init()
        initialized = True

    schedule.run_pending()  # check if we should nuke the cache and rebuild

    return generate_recommendations(combinedModel)


# noinspection PyPep8Naming
def generate_recommendations(combinedModel: CombinedModel) -> CombinedModel:
    count = 0

    for anchor in combinedModel.semanticModel.nodes:
        if type(anchor) == Literal or type(anchor) == NamedEntity:
            continue

        extensions = lme.generate_lme(anchor=anchor.uri, limit=10)
        recommendations = []
        for (s, p, o), c in extensions:
            if p.startswith(str(PLSM)) or p.startswith(str(PLCM)) or o.startswith(str(PLSM)) or o.startswith(str(PLCM)):
                continue
            delta: DeltaModification = DeltaModification()
            delta.reference = f"ARS-R-SR-ref-{str(count)}"
            delta.anchors = [anchor.uuid]
            delta.confidence = c
            count += 1

            o_smn = None

            # check if a similar object already exists
            for node in combinedModel.semanticModel.nodes:
                if node.uri == o:
                    o_smn = node
                    break

            if o_smn:
                existing = False  # needed to break two loops
                for edge in combinedModel.semanticModel.edges:
                    if edge.from_ == anchor.uuid and edge.to == o_smn.uuid and edge.uri == p:
                        existing = True
                        break
                if existing:
                    continue  # ignore all recommendations that already exist

            if not o_smn:
                if o.startswith(str(XSD)):
                    # deal with the literal
                    lit = Literal.parse_obj({
                        '_class': 'Literal',
                        'template': True,
                        'uri': o,
                        'label': o.removeprefix(str(XSD)),
                        'value': o.removeprefix(str(XSD)),
                        'x': anchor.x + 50,
                        'y': anchor.y - 150,
                        'uuid': str(uuid.uuid1())
                    })

                    o_smn = lit

                else:
                    # convert object node
                    label, comment = get_meta_info(o)
                    o_smn = SemanticModelNode.parse_obj(
                        {'_class': 'Class',
                         'uri': o,
                         'label': label,
                         'description': comment,
                         'x': anchor.x + 50,
                         'y': anchor.y - 150,
                         'uuid': str(uuid.uuid1())})

            delta.entities = [o_smn]

            # convert relation
            label, comment = get_meta_info(p)
            op: Relation = Relation.parse_obj(
                {'_class': 'DataProperty' if type(o_smn) == Literal else 'ObjectProperty',
                 'uri': p,
                 'label': label,
                 'description': comment,
                 'from': anchor.uuid,
                 'to': o_smn.uuid})
            delta.relations = [op]

            recommendations.append(delta)

        # print("recommended for", anchor.uri, recommendations)
        for recommendation in recommendations[:5]:
            combinedModel.recommendations.append(recommendation)
    return combinedModel


def get_meta_info(uri: str):
    if uri not in meta_info:
        return None, None
    return meta_info[uri]['label'], meta_info[uri]['comment']


def reset():
    global initialized
    lme.clear_cache()
    initialized = False


def init():
    # dirty hack to refresh the cache once per day.
    # has to be executed from inside the uvicorn runtime, so bound to request
    # will delay the specific request until cache is rebuild
    schedule.clear()  # prevent duplicate registrations
    print("scheduling reset to", resettime)
    schedule.every().day.at(resettime).do(reset)
    global meta_info
    lme.init()
    meta_info = modelbuilder.modelbuilder.build_label_index()


if __name__ == "__main__":
    port: int = int(os.getenv("SERVICE_PORT"))
    if not port:
        print("INFO", "No service port specified via 'SERVICE_PORT' env variable. Defaulting to 8221.")
        port = 8221
    uvicorn.run("service:app", host="0.0.0.0", port=port, reload=True)
