import { Component, Input } from '@angular/core';
import { PrimitiveNode } from '../../../api/generated/dms';

@Component({
  selector: 'pls-syntax-node-details',
  templateUrl: 'syntax-node.component.html',
  styleUrls: ['../common/dialog.styles.scss']
})
export class PlsSyntaxNodeDetailsComponent {

  @Input() data: PrimitiveNode;

  constructor() {
  }

}
