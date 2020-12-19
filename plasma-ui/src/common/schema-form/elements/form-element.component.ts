import { Component, Input } from '@angular/core';

import { FormElement } from '../interfaces/FormElement';

enum FormElementTypes {
  Text,
  Hidden,
  Checkbox,
  Complex
}

@Component({
  selector: 'app-schema-form-element',
  templateUrl: 'form-element.component.html'
})
export class FormElementComponent<T> {

  public readonly FormElementTypes = FormElementTypes;

  @Input()
  public formElement: FormElement<T>;

  public getFormElement() {
    if (this.formElement.hidden) {
      return FormElementTypes.Hidden;
    }

    switch (this.formElement.type) {
      case 'Complex':
        return FormElementTypes.Complex;
      case 'DataType':
        return FormElementTypes.Checkbox;
      default:
        return FormElementTypes.Text;
    }
  }

  public getCustomConfiguration() {
    switch (this.formElement.type) {
      case 'DataType':
        return {
          values: [
            'Unknown',
            'String',
            'Boolean',
            'Number',
            'Binary'
          ]
        };
      default:
        return {};
    }
  }

}
