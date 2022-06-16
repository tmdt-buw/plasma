import { Component, Input } from '@angular/core';
import { TypeDefinitionDTOObject } from '../../../../../../api/generated/dms';

@Component({
  selector: 'pls-schema-form-hidden-form-element',
  template: ''
})
export class PlsHiddenFormElementComponent {

  @Input()
  public formElement: TypeDefinitionDTOObject;

}
