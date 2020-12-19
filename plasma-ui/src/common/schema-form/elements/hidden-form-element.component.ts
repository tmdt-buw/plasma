import { Component, Input } from '@angular/core';

import { FormElement } from '../interfaces/FormElement';

@Component({
  selector: 'app-schema-form-hidden-form-element',
  template: ''
})
export class HiddenFormElementComponent<T> {

  @Input()
  public formElement: FormElement<T>;

}
