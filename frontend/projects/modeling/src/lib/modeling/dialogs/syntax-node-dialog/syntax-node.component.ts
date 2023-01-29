import { Component, Input } from '@angular/core';
import { PrimitiveNode, SchemaNode } from '../../../api/generated/dms';

@Component({
  selector: 'pls-syntax-node-details',
  templateUrl: 'syntax-node.component.html',
  styleUrls: ['../common/dialog.styles.scss']
})
export class PlsSyntaxNodeDetailsComponent {

  @Input() node: SchemaNode;

  constructor() {
  }

  getDescription(): string {
    if (this.node._class === 'ObjectNode' && this.node.pathAsJSONPointer === '/') {
      return 'The ROOT node is the beginning syntax node of your data source.';
    }
    if (this.node._class === 'SetNode') {
      return 'ArrayNodes represent an array in the original data. If this instance of PLASMA supports ';
    }
    if (this.node._class === 'ObjectNode') {
      return 'ObjectNode';
    }
    if (this.node._class === 'PrimitiveNode') {
      return 'PrimitiveNode';
    }
    return 'Unknown Node Type identified!';

  }

  getExamples(): string[] {
    return this.node?._class === 'PrimitiveNode' ? (this.node as PrimitiveNode).examples : [];
  }

  hasExamples(): boolean {
    return this.getExamples().length > 0;
  }

  getLabel(): string {
    if (this.node._class === 'ObjectNode' && this.node.pathAsJSONPointer === '/') {
      return 'Root object node';
    }
    if (this.node._class === 'SetNode') {
      return 'Array Node';
    }
    if (this.node._class === 'ObjectNode') {
      return 'Object Node';
    }
    if (this.node._class === 'PrimitiveNode') {
      return 'Primitive Node';
    }
    return 'Unknown Node Type';
  }
}
