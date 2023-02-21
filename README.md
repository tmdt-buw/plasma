# PLASMA
## Platform for Auxiliary Semantic Modeling Approaches

PLASMA is a modular framework for semantic model creation. It features a GUI that allows users to create semantic models
without requiring deep knowledge about technologies and formalisms.
Additionally, PLASMA provides interfaces to attach additional (auxiliary) services to support the user during the modeling process.


## Architecture
PLASMA currently consists of the following (micro)services: 

* **Data Modeling Service**: Service for semantic model creation 
* **Knowledge Graph Service**: Stores ontologies and created semantic models
* **Schema Analysis Service**: Analyses sample data to extract a syntactic schema on which the semantic model can be built
* **Semantic Recommendation Service**: Connecting service for all auxiliary services
* **Data Processing Service**: Processes files and converts JSON data to RDF
* Semantic Modeling UI: A modeling UI with focus on simplifying the modeling process
* Discovery Service: Enables service discovery using Eureka
* Spring Boot Admin Service: Monitoring
* Gateway Service: Offers a single entry point for all components

### Storage
PLASMA is preconfigured to use MariaDB instances for the raw data management.
Semantic information (the Knowledge Graph) is stored using Apache Jena Fuseki.

### Software and Frameworks
PLASMA is written in Java, using Java 11 as the baseline version. 
The included web-based frontend is written using Angular 13.

## Building PLASMA

### Requirements
* Install OpenJDK 11
* Install Docker for executing each service
* You require Maven to build the services
* The frontend uses npm to build. Check the dedicated frontend README for more information.

### Docker Setup
* Checkout the repository
* To build the services execute `mvn package` on the parent module in the `components` folder
* Alternatively, to also build the Docker containers execute `mvn package jib:dockerBuild -DskipTests` on the parent
  module

**Note**: Ensure that your docker is running on the local machine and can be reached

* Build the UI by running `npm run build:dms` and `npm run build` in `frontend` directory
* Build the docker container for the UI using the corresponding `Dockerfile` in `frontend`

### Starting PLASMA
* Optional: Adjust environment variables in the docker-compose.yml file. 
For an explanation of most parameters, check the individual README files of those services
* To start, execute the `docker-compose` script, e.g. `docker-compose up -d`
* Access the UI at `localhost:80`

### Stopping PLASMA

* PLASMA can be stopped by halting all Docker containers, e.g. `docker-compose down`

## Development Setup

If components are run locally, e.g. for debugging purposes, entries for each service have to be added to the /etc/hosts
file in the form of ``127.0.0.1 <container-hostname-in-docker-compose>``. Setting those lookups enables a local
component to communicate with other components that may still run in Docker. This is required as the Docker hostnames
are not available to services outside the Docker network.

Example:

```
127.0.0.1 fuseki
127.0.0.1 plasma-kgs
```

**Note**: This is only required when running the specific component(s) outside of Docker containers
(e.g. for debugging). Alternatively, the service environment may be edited and the reference URL of the resource changed
to ``localhost``.

### Default port mappings

These port mappings are only relevant when accessing the services via localhost. Inside the Docker network, all services
have their own host. Adjust the Docker port mappings if a deviation is needed (e.g. for testing). This list should give
an overview of the services and ports occupied and is for information purposes only.

```
Infrastructure
------------------
Discovery     8761
Spring Admin  8080
Gateway       8888

Databases
------------------
SAS           3307
DMS           3308

Components
------------------
DMS           8186
KGS           8181
SRS           8190
SAS           8189
DPS           8193
Frontend      80
           
Auxiliary Services
------------------
ARS-L-LM      8201
ARS-R-SR      8221
```






