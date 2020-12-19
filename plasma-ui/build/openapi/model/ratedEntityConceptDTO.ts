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
import { RepresentativeDTO } from './representativeDTO';


export interface RatedEntityConceptDTO {
    id?: string;
    mainLabel?: string;
    description?: string;
    synonyms?: Array<string>;
    keywords?: Array<string>;
    source?: string;
    idInDatasource?: string;
    score?: number;
    relatedConcepts?: Array<string>;
    representatives?: Array<RepresentativeDTO>;
}

