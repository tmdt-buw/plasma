import {DatasourcesComponent} from './datasources/datasources.component';
import {SchemaanalysisComponent} from './schemaanalysis/schemaanalysis.component';
import {ModelingComponent} from './modeling/modeling.component';
import {KgimportComponent} from './kgimport/kgimport.component';
import {KgexportComponent} from './kgexport/kgexport.component';

export const PLASMA_ROUTES = [
  {path: 'datasources', component: DatasourcesComponent},
  {path: 'schemaanalysis', component: SchemaanalysisComponent},
  {path: 'modeling', component: ModelingComponent},
  {path: 'kgimport', component: KgimportComponent},
  {path: 'kgexport', component: KgexportComponent},
];
