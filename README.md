# PLASMA

PLASMA is a modular framework for developing and evaluating semantic labeling, modeling and refinement approaches. Alltogether, PLASMA currently consists of the following microservices: 

* Discovery Service 
* Spring Boot Admin Service
* Gateway Service
* Semantic Database Service
* Knowledge Graph Service
* Schema Analysis Service
* Data Modeling Service 
* Semantic Recommendation Service
* Data Source Service 
* Auxiliary for Semantic Labeling

Each of these services takes over a specific task as described in our corresponding ESWC Paper. 

## Java Version
The java version of the service artifact is 11.  
The java version of the shared artifact ist 8.

## Development Preparation
* Install OpenJDK 11
* Install Docker for executing each service
* You require Maven to build the services
* We recommend IntelliJ as an IDE

## Development and Execution of the Backend Components
* Checkout the repository
* For building the backend components execute ```maven package``` on the parent module
* For installing the backend components including docker containers execute ```maven install``` on the parent module
** Ensure that your docker is running on the local machine and can be reached

## Development and Execution of the UI

## Execution
* After building all docker images, just execute the ```docker-compose``` script
* You can access the UI at ```localhost:80```
