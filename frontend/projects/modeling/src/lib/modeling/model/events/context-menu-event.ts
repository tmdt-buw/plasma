import { Relation, SemanticModelNode, SyntacticOperationDTO } from '../../../api/generated/dms';

export class ContextMenuEvent {
  type: ContextMenuEventType;
  target: SemanticModelNode | Relation;
  operation?: SyntacticOperationDTO;

  constructor(type: ContextMenuEventType, template: SemanticModelNode | Relation, operation?: SyntacticOperationDTO) {
    this.type = type;
    this.target = template;
    this.operation = operation;
  }
}

export enum ContextMenuEventType {
  removeRelation, removeEntity, performOperation, updateEntity
}
