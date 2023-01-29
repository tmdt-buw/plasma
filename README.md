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
If components are run locally, e.g. for debugging purposes, entries for each service have to be added to the /etc/hosts file.
Setting those lookups enables a local component to communicate with other components that may still run in Docker.
This is required due to a limitation of docker. Example:
```
127.0.0.1 plasma-discovery-service
127.0.0.1 plasma-admin-service
127.0.0.1 plasma-gateway-service
127.0.0.1 plasma-kgs
127.0.0.1 plasma-sas
127.0.0.1 plasma-dms
127.0.0.1 plasma-srs
127.0.0.1 plasma-dps
```

**Note**: This is only required when developing or running one or more component(s) outside of Docker.



