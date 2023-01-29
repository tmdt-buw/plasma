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
import { CombinedModelProvisionalRelationsInner } from './combinedModelProvisionalRelationsInner';
import { SemanticModel } from './semanticModel';
import { SemanticModelNode } from './semanticModelNode';
import { SyntaxModel } from './syntaxModel';
import { DeltaModification } from './deltaModification';


export interface CombinedModel {
  id?: string;
  syntaxModel?: SyntaxModel;
  semanticModel?: SemanticModel;
  recommendations?: Array<DeltaModification>;
  lastModification?: DeltaModification;
  finalized?: boolean;
  provisionalElements?: Array<SemanticModelNode>;
  provisionalRelations?: Array<CombinedModelProvisionalRelationsInner>;
  arrayContexts?: Array<SemanticModel>;
}

