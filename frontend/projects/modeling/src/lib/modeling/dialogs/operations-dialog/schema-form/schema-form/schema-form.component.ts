import { Component, EventEmitter, Input, Output } from '@angular/core';
import { TypeDefinitionDTOObject } from '../../../../../api/generated/dms';

@Component({
  selector: 'pls-schema-form',
  templateUrl: 'schema-form.component.html'
})
export class PlsSchemaFormComponent<T> {

  @Input()
  public formElement: TypeDefinitionDTOObject;

  // tslint:disable-next-line:no-output-native
  @Output()
  public submit: EventEmitter<TypeDefinitionDTOObject> = new EventEmitter();

  public onSubmit(): void {
    this.submit.emit(this.formElement);
  }

}
