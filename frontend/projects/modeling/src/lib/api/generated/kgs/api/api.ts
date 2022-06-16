export * from './ontologyController.service';
import { OntologyControllerService } from './ontologyController.service';
export * from './semanticModelController.service';
import { SemanticModelControllerService } from './semanticModelController.service';
export const APIS = [OntologyControllerService, SemanticModelControllerService];
