import { Component, Input, OnInit } from '@angular/core';
import { TypeDefinitionDTOObject } from '../../../../../../api/generated/dms';

@Component({
  selector: 'pls-schema-form-complex-form-element',
  templateUrl: 'complex-form-element.component.html'
})
export class PlsComplexFormElementComponent implements OnInit {

  @Input()
  public formElement: TypeDefinitionDTOObject;

  public ngOnInit(): void {
    if (this.formElement.minCardinality !== 1 || this.formElement.maxCardinality !== 1) {
      throw new Error('Middleware violated assumption that complex datatypes have min/max cardinality of 1.');
    }
    for (const element of this.formElement.value) {
      if (element instanceof Array) {
        throw new Error('Middleware violated assumption that complex datatypes dont have doubly nested values.');
      }
    }
  }

}
