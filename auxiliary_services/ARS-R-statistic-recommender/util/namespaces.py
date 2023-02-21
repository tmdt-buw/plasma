import os

from rdflib import RDF, RDFS, OWL, URIRef, Graph, Literal, XSD, SDO, Namespace

PLSM = Namespace("http://plasma.uni-wuppertal.de/sm/")
PLCM = Namespace("http://plasma.uni-wuppertal.de/cm#")
PLASMA = Namespace("http://plasma.uni-wuppertal.de/schema#")

PREFIX_MAP = {'rdf': str(RDF),
              'rdfs': str(RDFS),
              'owl': str(OWL),
              'schema': str(SDO),
              'xsd': str(XSD),
              'plsm': 'http://plasma.uni-wuppertal.de/sm/',
              'plasma': 'http://plasma.uni-wuppertal.de/schema#',
              'plcm': 'http://plasma.uni-wuppertal.de/cm#'}

PREFIXES = ''.join([f"PREFIX {item[0]}: {URIRef(item[1]).n3()}{os.linesep}" for item in PREFIX_MAP.items()])