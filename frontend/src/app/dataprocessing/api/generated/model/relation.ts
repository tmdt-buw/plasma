/**
 * PLASMA DPS Service
 * Provides data processing capabilities to PLASMA
 *
 * The version of the OpenAPI document: 1.4.0
 * Contact: paulus@uni-wuppertal.de
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


export interface Relation {
  uuid?: string;
  from?: string;
  to?: string;
  uri?: string;
  label?: string;
  description?: string;
  properties?: Array<string>;
  arraycontext?: boolean;
  provisional?: boolean;
  template?: boolean;
  _class: string;
}

