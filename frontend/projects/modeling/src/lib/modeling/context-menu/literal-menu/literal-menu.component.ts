import { Component, Input } from '@angular/core';
import { Literal } from '../../../api/generated/dms';
import { ContextMenuEvent, ContextMenuEventType } from '../../model/events/context-menu-event';
import { ContextMenuService } from '../context-menu.service';

@Component({
  selector: 'pls-literal-menu',
  templateUrl: 'literal-menu.component.html',
  styleUrls: ['../context-menu.less']
})
export class PlsLiteralMenuComponent {

  @Input() node: Literal;

  constructor(private contextMenu: ContextMenuService) {
  }

  remove(): void {
    this.contextMenu.close(new ContextMenuEvent(ContextMenuEventType.removeEntity, this.node));
  }
}
