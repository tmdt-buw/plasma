import { Component, Input, OnInit } from '@angular/core';
import { TypeDefinitionDTOObject } from '../../../../../../api/generated/dms';


@Component({
  selector: 'pls-schema-form-checkbox-form-element',
  templateUrl: 'checkbox-form-element.component.html'
})
export class PlsCheckboxFormElementComponent implements OnInit {

  @Input()
  public formElement: TypeDefinitionDTOObject;

  @Input()
  public customConfiguration: { values: string[] };

  static getDataTypeIcon(value: string): string {
    switch (value) {
      case 'Number':
        return '&#xf292;';
      case 'String':
        return '&#xf031;';
      case 'Boolean':
        return '&#xf042;';
      case 'Binary':
        return '&#xf15b;';
      case 'Unknown':
        return '&#xf128;';
      default:
        return '';
    }
  }

  private static duplicateValueEntry(): any {
    return '' as any;
  }

  public ngOnInit(): void {
    for (let i = this.formElement.value.length; i < this.formElement.minCardinality; i++) {
      this.formElement.value.push(PlsCheckboxFormElementComponent.duplicateValueEntry());
    }
  }

  public addElement(event: Event): void {
    event.preventDefault();
    if (this.formElement.value.length === this.formElement.maxCardinality) {
      throw Error(`Cannot add instance of "${this.formElement.label}" since maximum cardinality is already reached.`);
    }
    this.formElement.value.push(PlsCheckboxFormElementComponent.duplicateValueEntry());
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

  public selectValue(value: any): void {
    if (this.formElement.value.indexOf(value) !== -1) {
      this.formElement.value.splice(this.formElement.value.indexOf(value), 1);
    } else {
      if (this.formElement.value.length === this.formElement.maxCardinality) {
        this.formElement.value.shift();
      }
      this.formElement.value.push(value);
    }
  }

  getDataTypeIcon(value: string): string {
    return PlsCheckboxFormElementComponent.getDataTypeIcon(value);
  }
}
