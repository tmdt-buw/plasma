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
import { Edge } from './edge';


export interface SyntaxModel { 
    root?: string;
    nodes?: Array<SchemaNode>;
    edges?: Array<Edge>;
}

