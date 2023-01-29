import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FlexLayoutModule } from '@angular/flex-layout';
import { SchemaAnalysisComponent } from './schema-analysis/schema-analysis.component';
import { JsonViewerComponent } from './json-viewer/json-viewer.component';
import { CdkTreeModule } from '@angular/cdk/tree';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCollapseModule } from 'ng-zorro-antd/collapse';
import { TextFieldModule } from '@angular/cdk/text-field';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzTypographyModule } from 'ng-zorro-antd/typography';
import { FormsModule} from '@angular/forms';
import { ApiModule, Configuration } from './api/generated';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { environment } from '../../environments/environment';

@NgModule({
  declarations: [SchemaAnalysisComponent, JsonViewerComponent],
  exports: [
    SchemaAnalysisComponent
  ],
  imports: [
    // Angular
    CommonModule,
    FlexLayoutModule,
    CdkTreeModule,
    NzButtonModule,
    NzCollapseModule,
    TextFieldModule,
    NzInputModule,
    NzIconModule,
    NzSpinModule,
    NzTypographyModule,
    FormsModule,
    ApiModule.forRoot(() => {
      return new Configuration({
        basePath: environment.baseUrl,
      });
    }),
    NzAlertModule,
    NzModalModule
  ]
})
export class SchemaAnalysisModule {
}
