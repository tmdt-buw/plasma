/**
 *
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 0.0.1
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
import { SplitterDTO } from './splitterDTO';
import { PrimitiveDTO } from './primitiveNodeDTO';


export interface CompositeNodeDTOAllOf {
    components?: Array<PrimitiveDTO>;
    splitter?: Array<SplitterDTO>;
    examples?: Array<string>;
    cleansing_pattern?: string;
}

