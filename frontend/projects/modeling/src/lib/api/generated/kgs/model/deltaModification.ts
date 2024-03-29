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
import { SchemaNode } from './schemaNode';
import { SemanticModelNode } from './semanticModelNode';
import { Edge } from './edge';
import { GetRelations200ResponseInner } from './getRelations200ResponseInner';


export interface DeltaModification { 
    reference?: string;
    deletion?: boolean;
    entities?: Array<SemanticModelNode>;
    relations?: Array<GetRelations200ResponseInner>;
    nodes?: Array<SchemaNode>;
    edges?: Array<Edge>;
    anchors?: Array<string>;
    confidence?: number;
}

