import {EntityRuleDTO} from './entityRuleDTO';
import {RelationRuleDTO} from './relationRuleDTO';

export interface RuleProvisionDTO {
  name?: string;
  description?: string;
  entityRules?: EntityRuleDTO[];
  relationRules?: RelationRuleDTO[];
}
