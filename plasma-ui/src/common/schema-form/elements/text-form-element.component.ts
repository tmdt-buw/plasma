import { Component, Input, OnInit } from '@angular/core';

import { FormElement } from '../interfaces/FormElement';

@Component({
  selector: 'app-schema-form-text-form-element',
  templateUrl: 'text-form-element.component.html'
})
export class TextFormElementComponent<T> implements OnInit {

  @Input()
  public formElement: FormElement<T>;

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

  private duplicateValueEntry(): T {
    return '' as any;
  }
}
