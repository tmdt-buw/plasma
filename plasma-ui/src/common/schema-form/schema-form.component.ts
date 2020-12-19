import { Component, EventEmitter, Input, Output } from '@angular/core';

import { FormElement } from './interfaces/FormElement';

@Component({
  selector: 'app-schema-form',
  templateUrl: 'schema-form.component.html'
})
export class SchemaFormComponent<T> {

  @Input()
  public formElement: FormElement<T>;

  @Output()
  public submit: EventEmitter<FormElement<T>> = new EventEmitter();

  public onSubmit(): void {
    this.submit.emit(this.formElement);
  }

}
