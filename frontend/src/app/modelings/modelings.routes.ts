import { ModelingsListComponent } from './modelings-list/modelings-list.component';
import { CreateModelingComponent } from './create-modeling/create-modeling.component';


export const MODELINGS_ROUTES = [
  {path: '', component: ModelingsListComponent},
  {path: 'create', component: CreateModelingComponent}
];
