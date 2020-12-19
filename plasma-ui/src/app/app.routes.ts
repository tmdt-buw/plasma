export const APP_ROUTES = [
  {path: '', loadChildren: './main/navigation/navigation.module#NavigationModule'},
  {path: '**', redirectTo: ''}
];
