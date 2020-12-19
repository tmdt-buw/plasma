/**
 * PLASMA Knowledge Graph Application
 * Service for maintaining the Bottom-up Knowledge Graph
 *
 * The version of the OpenAPI document: 1.9.5
 * Contact: pomp@uni-wuppertal.de
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
import { DataStoringDTO } from './dataStoringDTO';


export interface MLBasedDTO {
    id?: string;
    dataStoring?: Array<DataStoringDTO>;
    path?: string;
    mlType?: string;
    type?: string;
}

