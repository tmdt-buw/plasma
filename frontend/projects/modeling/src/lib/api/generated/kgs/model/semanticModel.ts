/**
 * PLASMA KGS Service
 * Service for maintaining the Knowledge Graph
 *
 * The version of the OpenAPI document: 1.4.0
 * Contact: pomp@uni-wuppertal.de
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
import { SemanticModelNode } from './semanticModelNode';
import { GetRelations200ResponseInner } from './getRelations200ResponseInner';


export interface SemanticModel { 
    id?: string;
    nodes?: Array<SemanticModelNode>;
    edges?: Array<GetRelations200ResponseInner>;
}

