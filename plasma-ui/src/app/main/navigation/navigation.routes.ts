import { navigationRoutesNames } from './navigation.routes.names';
import { NavigationComponent } from './navigation/navigation.component';

export const NAVIGATION_ROUTES = [
  {
    path: '', component: NavigationComponent, children: [
      {path: '', redirectTo: navigationRoutesNames.PLASMA, pathMatch: 'full'},
      {path: navigationRoutesNames.PLASMA, loadChildren: 'src/app/plasma/plasma.module#PlasmaModule'}
    ]
  }
];

