### Platform for Auxiliary Semantic Modeling Approaches (PLASMA)

# Knowledge Graph Service

This service maintains and stores the knowledge base as well as ontologies made available to the system.

## Environment Variables
This section covers the main environment variables that can be manipulated in order to customize PLASMA.
Set them in the docker-compose environment section to overwrite container set values.

### Local ontology configuration
plasma.kgs.localontology.prefix=local
plasma.kgs.localontology.uri=https://local.ontology#
plasma.kgs.localontology.label=LOCAL
plasma.kgs.localontology.description=A local ontology maintained by this instance.
