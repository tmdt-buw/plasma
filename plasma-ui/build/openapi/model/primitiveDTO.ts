/**
 * PLASMA Data Modeling Application
 * Service for modeling data sources
 *
 * OpenAPI spec version: 0.1.3
 * Contact: pomp@uni-wuppertal.de
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */
import { EntityConceptSuggestionDTO } from './entityConceptSuggestionDTO';
import { SchemaNodeDTO } from './schemaNodeDTO';

export interface PrimitiveDTO extends SchemaNodeDTO {
  dataType?: string;
  suggestedEntityConcepts?: Array<EntityConceptSuggestionDTO>;
  examples?: string[];
  cleansingPattern?: string;
}
