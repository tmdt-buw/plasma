import { NgModule } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ModelingsListComponent } from './modelings-list/modelings-list.component';
import { NzTableModule } from 'ng-zorro-antd/table';
import { FlexModule } from '@angular/flex-layout';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { RouterModule } from '@angular/router';
import { MODELINGS_ROUTES } from './modelings.routes';
import { ApiModule, Configuration } from '@tmdt/dms';
import { CreateModelingComponent } from './create-modeling/create-modeling.component';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { SchemaAnalysisModule } from '../schema-analysis/schema-analysis.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NzNotificationModule } from 'ng-zorro-antd/notification';
import { NzTypographyModule } from 'ng-zorro-antd/typography';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { NzPopconfirmModule } from 'ng-zorro-antd/popconfirm';
import { NzPageHeaderModule } from 'ng-zorro-antd/page-header';
import { DataprocessingModule } from '../dataprocessing/dataprocessing.module';
import { NzUploadModule } from 'ng-zorro-antd/upload';
import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { environment } from '../../environments/environment';
import { EditModelingComponent } from './edit-modeling/edit-modeling.component';

@NgModule({
  declarations: [
    ModelingsListComponent,
    CreateModelingComponent,
    EditModelingComponent
  ],
  imports: [
    CommonModule,
    NzTableModule,
    FlexModule,
    NzButtonModule,
    NzIconModule,
        ApiModule.forRoot(() => {
          return new Configuration({
            basePath: environment.baseUrl,
          });
        }),
    RouterModule.forChild(MODELINGS_ROUTES),
    NzAlertModule,
    NzFormModule,
    NzInputModule,
    SchemaAnalysisModule,
    DataprocessingModule,
    FormsModule,
    ReactiveFormsModule,
    NzNotificationModule,
    NzTypographyModule,
    NzModalModule,
    NzPopconfirmModule,
    NzPageHeaderModule,
    NzUploadModule,
    NzDropDownModule,
    NzToolTipModule
  ],
  providers: [
    DatePipe
  ]
})
export class ModelingsModule { }
