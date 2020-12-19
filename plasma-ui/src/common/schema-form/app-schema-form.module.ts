/* tslint:disable:max-line-length */
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { CheckboxFormElementComponent } from './elements/checkbox-form-element.component';
import { ComplexFormElementComponent } from './elements/complex-form-element.component';
import { FormElementComponent } from './elements/form-element.component';
import { HiddenFormElementComponent } from './elements/hidden-form-element.component';
import { TextFormElementComponent } from './elements/text-form-element.component';
import { SchemaFormComponent } from './schema-form.component';

@NgModule({
  declarations: [
    FormElementComponent,
    CheckboxFormElementComponent,
    ComplexFormElementComponent,
    TextFormElementComponent,
    HiddenFormElementComponent,
    SchemaFormComponent
  ],
  imports: [
    CommonModule,
    FormsModule
  ],
  exports: [
    SchemaFormComponent
  ],
  providers: []
})

export class AppSchemaFormModule {
}
