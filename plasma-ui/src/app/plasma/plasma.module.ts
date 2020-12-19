import {NgModule} from '@angular/core';
import {SharedModule} from '../shared-module';
import {RouterModule} from '@angular/router';
import {PLASMA_ROUTES} from './plasma.routes';
import { DatasourcesComponent } from './datasources/datasources.component';
import { SchemaanalysisComponent } from './schemaanalysis/schemaanalysis.component';
import {ModelingComponent} from './modeling/modeling.component';
import { InformationgraphComponent } from './modeling/informationgraph/informationgraph.component';

import {NgbAlertModule, NgbModalConfig, NgbModule, NgbTooltipModule} from '@ng-bootstrap/ng-bootstrap';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';

import {NgSelectizeModule} from 'ng-selectize';
import {AppVisModule} from '../../common/vis/app-vis.module';
import {AppSchemaFormModule} from '../../common/schema-form/app-schema-form.module';
import { KgimportComponent } from './kgimport/kgimport.component';
import { KgexportComponent } from './kgexport/kgexport.component';


@NgModule({
  bootstrap: [
    ModelingComponent
  ],
  declarations: [
    DatasourcesComponent,
    SchemaanalysisComponent,
    ModelingComponent,
    InformationgraphComponent,
    KgimportComponent,
    KgexportComponent],
  imports: [
    SharedModule,
    RouterModule.forChild(PLASMA_ROUTES),
    NgbTooltipModule,
    NgbAlertModule,
    NgbModule,
    FormsModule,
    ReactiveFormsModule,
    NgSelectizeModule,
    AppVisModule,
    AppSchemaFormModule,
  ],
  providers: [
    {
      provide: NgbModalConfig,
      useFactory: () => {
        const config = new NgbModalConfig();
        config.backdrop = 'static';
        return config;
      }
    }
  ]
})
export class PlasmaModule {
}
