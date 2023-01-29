import { Component, Input } from '@angular/core';
import { Relation } from '../../../api/generated/dms';
import { ContextMenuService } from '../context-menu.service';
import { ContextMenuEvent, ContextMenuEventType } from '../../model/events/context-menu-event';

@Component({
  selector: 'pls-relation-concept-menu',
  templateUrl: 'relation-menu.component.html',
  styleUrls: ['../context-menu.less']
})
export class PlsRelationConceptMenuComponent {

  @Input() relation: Relation;
  @Input() arrayContextAvailable: boolean = false;

  constructor(private contextMenu: ContextMenuService) {
  }

  remove(): void {
    this.contextMenu.close(new ContextMenuEvent(ContextMenuEventType.removeRelation, this.relation));
  }

  removeFromArrayContext(): void {
    this.relation.arraycontext = false;
    this.contextMenu.close(new ContextMenuEvent(ContextMenuEventType.updateRelation, this.relation));
  }

  addToArrayContext(): void {
    this.relation.arraycontext = true;
    this.contextMenu.close(new ContextMenuEvent(ContextMenuEventType.updateRelation, this.relation));
  }
}
