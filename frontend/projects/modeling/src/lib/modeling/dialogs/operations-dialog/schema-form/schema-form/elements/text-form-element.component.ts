import { Component, Input, OnInit } from '@angular/core';
import { TypeDefinitionDTOObject } from '../../../../../../api/generated/dms';

@Component({
  selector: 'pls-schema-form-text-form-element',
  templateUrl: 'text-form-element.component.html'
})
export class PlsTextFormElementComponent implements OnInit {

  @Input()
  public formElement: TypeDefinitionDTOObject;

  private static duplicateValueEntry(): any {
    return '' as any;
  }

  public ngOnInit(): void {
    for (let i = this.formElement.value.length; i < this.formElement.minCardinality; i++) {
      this.formElement.value.push(PlsTextFormElementComponent.duplicateValueEntry());
    }
  }

  public addElement(event: Event): void {
    event.preventDefault();
    if (this.formElement.value.length === this.formElement.maxCardinality) {
      throw Error(`Cannot add instance of "${this.formElement.label}" since maximum cardinality is already reached.`);
    }
    this.formElement.value.push(PlsTextFormElementComponent.duplicateValueEntry());
  }

  public removeElement(index: number): void {
    if (this.formElement.value.length === this.formElement.minCardinality) {
      throw Error(`Cannot remove instance of "${this.formElement.label}" since minimum cardinality is already reached.`);
    }
    this.formElement.value.splice(index, 1);
  }

  public trackByFn(index: number, item: any): number {
    return index;
  }
}
