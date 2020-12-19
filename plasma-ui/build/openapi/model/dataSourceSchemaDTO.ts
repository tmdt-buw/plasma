/**
 * PLASMA Data Modeling Application
 * Service for modeling data sources
 *
 * OpenAPI spec version: 0.1.3
 * Contact: pomp@uni-wuppertal.de
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */
import { PrimitiveEntityTypeEdgeDTO } from './primitiveEntityTypeEdgeDTO';
import { SemanticModelDTO } from './semanticModelDTO';
import { SyntaxModelDTO } from './syntaxModelDTO';

export interface DataSourceSchemaDTO {
    syntaxModel?: SyntaxModelDTO;
    semanticModel?: SemanticModelDTO;
    primitiveToEntityTypeList?: Array<PrimitiveEntityTypeEdgeDTO>;
    finalized?: boolean;
}
