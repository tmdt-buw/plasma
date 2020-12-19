import { Component, Input, OnInit } from '@angular/core';

import { FormElement } from '../interfaces/FormElement';

@Component({
  selector: 'app-schema-form-checkbox-form-element',
  templateUrl: 'checkbox-form-element.component.html'
})
export class CheckboxFormElementComponent<T> implements OnInit {

  @Input()
  public formElement: FormElement<T>;

  @Input()
  public customConfiguration: { values: string[] };

  static getDataTypeIcon(value: string) {
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

  public ngOnInit() {
    for (let i = this.formElement.value.length; i < this.formElement.minimumCardinality; i++) {
      this.formElement.value.push(this.duplicateValueEntry());
    }
  }

  public addElement(event: Event) {
    event.preventDefault();
    if (this.formElement.value.length === this.formElement.maximumCardinality) {
      throw Error(`Cannot add instance of "${this.formElement.label}" since maximum cardinality is already reached.`);
    }
    this.formElement.value.push(this.duplicateValueEntry());
  }

  public removeElement(index: number) {
    if (this.formElement.value.length === this.formElement.minimumCardinality) {
      throw Error(`Cannot remove instance of "${this.formElement.label}" since minimum cardinality is already reached.`);
    }
    this.formElement.value.splice(index, 1);
  }

  public trackByFn(index: any, item: any) {
    return index;
  }

  public selectValue(value: T) {
    if (this.formElement.value.indexOf(value) !== -1) {
      this.formElement.value.splice(this.formElement.value.indexOf(value), 1);
    } else {
      if (this.formElement.value.length === this.formElement.maximumCardinality) {
        this.formElement.value.shift();
      }
      this.formElement.value.push(value);
    }
  }

  private duplicateValueEntry(): T {
    return '' as any;
  }

  private getDataTypeIcon(value: string) {
    return CheckboxFormElementComponent.getDataTypeIcon(value);
  }
}
