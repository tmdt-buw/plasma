import { SemanticModelNode } from '../../../api/generated/dms';
import { NodeType, SyntaxNode } from '../configuration/node';

export class NodeChangedEvent {
  type: NodeType;
  node: SyntaxNode | SemanticModelNode;

  constructor(type: NodeType, node: SyntaxNode | SemanticModelNode) {
    this.type = type;
    this.node = node;
  }
}
