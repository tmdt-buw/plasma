import { Component, Input, OnInit } from '@angular/core';

import { FormElement } from '../interfaces/FormElement';

@Component({
  selector: 'app-schema-form-complex-form-element',
  templateUrl: 'complex-form-element.component.html'
})
export class ComplexFormElementComponent<T> implements OnInit {

  @Input()
  public formElement: FormElement<T>;

  public ngOnInit() {
    if (this.formElement.minimumCardinality !== 1 || this.formElement.maximumCardinality !== 1) {
      throw new Error('Middleware violated assumption that complex datatypes have min/max cardinality of 1.');
    }
    for (const element of this.formElement.value) {
      if (element instanceof Array) {
        throw new Error('Middleware violated assumption that complex datatypes dont have doubly nested values.');
      }
    }
  }

}
