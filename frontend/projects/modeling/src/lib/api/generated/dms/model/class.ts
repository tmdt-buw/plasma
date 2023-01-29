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
import { Instance } from './instance';
import { PositionedCombinedModelElement } from './positionedCombinedModelElement';


export interface Class extends PositionedCombinedModelElement {
  uri?: string;
  description?: string;
  syntaxNodeUuid?: string;
  syntaxLabel?: string;
  syntaxPath?: string;
  instance?: Instance;
  provisional?: boolean;
  mapped?: boolean;
  template?: boolean;
}

