import { Component, Input } from '@angular/core';
import { NamedEntity, SemanticModelNode, SyntacticOperationDTO } from '../../../api/generated/dms';
import { ContextMenuEvent, ContextMenuEventType } from '../../model/events/context-menu-event';
import { ContextMenuService } from '../context-menu.service';
import { SyntaxNode } from '../../model/configuration/node';

@Component({
  selector: 'pls-named-entity-menu',
  templateUrl: 'named-entity-menu.component.html',
  styleUrls: ['../context-menu.less']
})
export class PlsNamedEntityMenuComponent {

  @Input() node: NamedEntity;

  constructor(private contextMenu: ContextMenuService) {
  }

  remove(): void {
    this.contextMenu.close(new ContextMenuEvent(ContextMenuEventType.removeEntity, this.node));
  }
}
