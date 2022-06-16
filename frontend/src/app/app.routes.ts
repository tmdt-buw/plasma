import { appRoutesNames } from './app.routes.names';

export const APP_ROUTES = [
  {path: '', redirectTo: appRoutesNames.MODELINGS, pathMatch: 'full'},
  {
    path: appRoutesNames.MODELINGS,
    loadChildren: () => import('src/app/modelings/modelings.module').then(m => m.ModelingsModule)
  },
  {
    path: appRoutesNames.MODELS,
    loadChildren: () => import('src/app/modeling/modeling.module').then(m => m.ModelingModule)
  }
];
