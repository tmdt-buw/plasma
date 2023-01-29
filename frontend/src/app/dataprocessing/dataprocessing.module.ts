import { NgModule } from '@angular/core';
import { ApiModule } from '../dataprocessing/api/generated';
import { ConversionDialogComponent } from './conversion-dialog/conversion-dialog.component';
import { FlexModule } from '@angular/flex-layout';
import { NzListModule } from 'ng-zorro-antd/list';
import { NzTypographyModule } from 'ng-zorro-antd/typography';
import { NzUploadModule } from 'ng-zorro-antd/upload';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { CommonModule } from '@angular/common';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { Configuration } from '../../../projects/modeling/src/lib/api/generated/dms';
import { environment } from '../../environments/environment';
import { ArraycontextInfoComponent } from './arraycontext-info/arraycontext-info.component';
import { NzPageHeaderModule } from 'ng-zorro-antd/page-header';
import { NzCardModule } from 'ng-zorro-antd/card';

@NgModule({
  declarations: [
    ConversionDialogComponent,
    ArraycontextInfoComponent
  ],
  exports: [],
  imports: [
    ApiModule.forRoot(() => {
      return new Configuration({
        basePath: environment.baseUrl,
      });
    }),
    FlexModule,
    NzListModule,
    NzTypographyModule,
    NzUploadModule,
    NzIconModule,
    CommonModule,
    NzButtonModule,
    NzPageHeaderModule,
    NzCardModule,
  ]
})
export class DataprocessingModule {
}
