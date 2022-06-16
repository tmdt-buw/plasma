import { NgModule } from '@angular/core';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzDividerModule } from 'ng-zorro-antd/divider';
import { PlsOperationsDialogComponent } from './operations-dialog/operations-dialog.component';
import { PlsSchemaFormModule } from './schema-form/schema-form.module';
import { CommonModule } from '@angular/common';
import { FlexLayoutModule } from '@angular/flex-layout';

@NgModule({
  declarations: [PlsOperationsDialogComponent],
  imports: [
    // Plasma
    PlsSchemaFormModule,
    // Angular
    CommonModule,
    FlexLayoutModule,
    NzDividerModule,
    NzAlertModule,
    NzButtonModule
  ],
  exports: [PlsOperationsDialogComponent]
})
export class PlsOperationsDialogModule {
}
