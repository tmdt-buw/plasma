import { Component } from '@angular/core';
import { animate, state, style, transition, trigger } from '@angular/animations';
import { navigationRoutesNames } from '../navigation.routes.names';

@Component({
  selector: 'app-navigation',
  templateUrl: './navigation.component.html',
  styleUrls: ['./navigation.component.scss'],
  animations: [
    trigger('sidenavExpand', [
      state('sidenav-collapsed', style({width: '56px'})),
      state('sidenav-expanded', style({width: '250px'})),
      state('content-collapsed', style({margin: '0 0 0 56px'})),
      state('content-expanded', style({margin: '0 0 0 250px'})),
      transition('sidenav-expanded <=> sidenav-collapsed', animate('.5s ease')),
      transition('content-expanded <=> content-collapsed', animate('.5s ease'))
    ])
  ]
})
export class NavigationComponent {

  // Routes
  readonly datasourcesRoute = '/' + navigationRoutesNames.DATASOURCES;
  readonly schemaanaylsisRoute = '/' + navigationRoutesNames.SCHEMAANALYSIS;
  readonly modelingRoute = '/' + navigationRoutesNames.MODELING;
  readonly kgImportRoute = '/' + navigationRoutesNames.KGIMPORT;
  readonly kgExportRoute = '/' + navigationRoutesNames.KGEXPORT;

  opened = true;

  constructor() { }

  toggle() {
    this.opened = !this.opened;
  }
}
