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
import { PositionedCombinedModelElement } from './positionedCombinedModelElement';


export interface CollisionSchema extends PositionedCombinedModelElement {
    path?: Array<string>;
    pathAsJSONPointer?: string;
    visible?: boolean;
    disabled?: boolean;
}

