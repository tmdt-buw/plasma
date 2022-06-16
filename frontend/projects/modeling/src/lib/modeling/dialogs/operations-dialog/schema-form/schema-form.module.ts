import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';

import {PlsCheckboxFormElementComponent} from './schema-form/elements/checkbox-form-element.component';
import {PlsComplexFormElementComponent} from './schema-form/elements/complex-form-element.component';
import {PlsFormElementComponent} from './schema-form/elements/form-element.component';
import {PlsHiddenFormElementComponent} from './schema-form/elements/hidden-form-element.component';
import {PlsTextFormElementComponent} from './schema-form/elements/text-form-element.component';
import {PlsSchemaFormComponent} from './schema-form/schema-form.component';
import {FlexLayoutModule} from '@angular/flex-layout';

@NgModule({
  declarations: [
    PlsFormElementComponent,
    PlsCheckboxFormElementComponent,
    PlsComplexFormElementComponent,
    PlsTextFormElementComponent,
    PlsHiddenFormElementComponent,
    PlsSchemaFormComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    FlexLayoutModule,
    NzButtonModule,
    NzIconModule
  ],
  exports: [
    PlsSchemaFormComponent
  ]
})

export class PlsSchemaFormModule {
}
