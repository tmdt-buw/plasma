import { Component, Input } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { NzModalRef } from 'ng-zorro-antd/modal';
import { Class, ModelingControllerService, NamedEntity } from '../../../api/generated/dms';

@Component({
  selector: 'pls-new-entity-dialog',
  templateUrl: './new-entity-dialog.component.html',
  styleUrls: ['../common/dialog.styles.scss']
})
export class PlsNewEntityDialogComponent {

  @Input() modelId: string;
  @Input() defaultNamespace: string = 'local';

  form = new FormGroup({
    label: new FormControl('', [Validators.required]),
    description: new FormControl('', [Validators.required]),
    namespace: new FormControl(this.defaultNamespace + ':', [Validators.required]),
    identifier: new FormControl(''),
    namedEntity: new FormControl(false)
  });

  error;
  isNamedEntity = false;

  constructor(private modalRef: NzModalRef, private modelingService: ModelingControllerService) {
  }

  submit(): void {
    this.error = null;

    if (this.form.get('label').value.trim() === '') {
      this.error = 'Empty label is not allowed';
      return;
    }
    let node: Class | NamedEntity;
    if (this.form.get('namedEntity').value) {
      node = {
        _class: 'NamedEntity',
        uri: this.constructURI(),
        description: this.form.get('description').value,
        label: this.form.get('label').value
      };
    } else {
      node = {
        _class: 'Class',
        uri: this.constructURI(),
        description: this.form.get('description').value,
        label: this.form.get('label').value
      };
    }
    this.modelingService.cacheElement(this.modelId, node).subscribe(() => this.modalRef.close(true), error => this.error = error.error.message);
  }

  cancel(): void {
    this.error = null;
    this.modalRef.close();
  }

  generateIdentifier(): string {
    return this.convertLabelToIdentifier(this.form.get('label').value);
  }

  convertLabelToIdentifier(input: string): string {
    return input.trim().replace(/[^a-zA-Z0-9]/g, '_');
  }

  constructURI(): string {
    let namespace = this.form.get('namespace').value;
    let identifier = this.form.get('identifier').value;

    if (!namespace.includes(':')) {
      namespace = namespace + ':';
    }
    if (!identifier) {
      identifier = this.generateIdentifier();
    }
    return namespace + identifier;
  }

  onIdentifierEdited(event: Event): void {
  }

  onNamespaceEdited(event: Event): void {
    let namespace = this.form.get('namespace').value;

    if (!namespace.includes(':')) {
      namespace = namespace + ':';
    }
    this.form.get('namespace').setValue(namespace);
  }

  onLabelEdited(event: Event): void {
  }
}
