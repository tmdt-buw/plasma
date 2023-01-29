/**
 * PLASMA DMS Service
 * Service for modeling data sources
 *
 * The version of the OpenAPI document: 1.5.0
 * Contact: pomp@uni-wuppertal.de
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
import { SyntacticOperationDTO } from './syntacticOperationDTO';


export interface PrimitiveNodeAllOf { 
    path?: Array<string>;
    valid?: boolean;
    datatype?: PrimitiveNodeAllOf.DatatypeEnum;
    examples?: Array<string>;
    pattern?: string;
    visible?: boolean;
    disabled?: boolean;
    pathAsJSONPointer?: string;
    readonly operations?: Array<SyntacticOperationDTO>;
}
export namespace PrimitiveNodeAllOf {
    export type DatatypeEnum = 'Unknown' | 'String' | 'Boolean' | 'Number' | 'Binary';
    export const DatatypeEnum = {
        Unknown: 'Unknown' as DatatypeEnum,
        String: 'String' as DatatypeEnum,
        Boolean: 'Boolean' as DatatypeEnum,
        Number: 'Number' as DatatypeEnum,
        Binary: 'Binary' as DatatypeEnum
    };
}


