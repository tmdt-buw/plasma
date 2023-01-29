import { ModelingsListComponent } from './modelings-list/modelings-list.component';
import { CreateModelingComponent } from './create-modeling/create-modeling.component';
import { ArraycontextInfoComponent } from '../dataprocessing/arraycontext-info/arraycontext-info.component';


export const MODELINGS_ROUTES = [
  {path: '', component: ModelingsListComponent},
  {path: 'create', component: CreateModelingComponent},
  {path: 'arraycontext-info', component: ArraycontextInfoComponent}
];
