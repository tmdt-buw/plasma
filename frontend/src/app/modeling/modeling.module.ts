import { NgModule } from '@angular/core';
import * as dms from '@tmdt/dms';
import { environment } from '../../environments/environment';
import { CommonModule } from '@angular/common';
import { FlexLayoutModule } from '@angular/flex-layout';
import { RouterModule } from '@angular/router';
import { MODELING_ROUTES } from './modeling.routes';
import { ModelingComponent } from './modeling/modeling.component';
import { NzPageHeaderModule } from 'ng-zorro-antd/page-header';


@NgModule({
  declarations: [ModelingComponent],
  imports: [
    // Plasma
    dms.PlsModelingModule.setBasePath(environment.baseUrl),
    // Angular
    CommonModule,
    FlexLayoutModule,
    RouterModule.forChild(MODELING_ROUTES),
    NzPageHeaderModule,
  ]
})
export class ModelingModule {
}
