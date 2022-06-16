import { Component, Input } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { NzModalRef } from 'ng-zorro-antd/modal';
import { ModelingControllerService, ObjectProperty } from '../../../api/generated/dms';

@Component({
  selector: 'pls-new-relation-dialog',
  templateUrl: './new-relation-dialog.component.html',
  styleUrls: ['../common/dialog.styles.scss']
})
export class PlsNewRelationDialogComponent {

  @Input() modelId: string;
  @Input() defaultNamespace: string = 'local';

  form = new FormGroup({
    label: new FormControl('', [Validators.required]),
    description: new FormControl('', [Validators.required]),
    namespace: new FormControl(this.defaultNamespace + ':', [Validators.required]),
    identifier: new FormControl(''),
    type: new FormControl('', [Validators.required])
  });

  error;
  relationType: 'ObjectProperty' | 'DataProperty';

  constructor(private modalRef: NzModalRef, private modelingService: ModelingControllerService) {
  }

  submit(): void {
    this.error = null;
    const rel: ObjectProperty = {
      _class: this.form.get('type').value,
      uri: this.constructURI(),
      label: this.form.get('label').value
    };
    console.log(rel);
    this.modelingService.cacheRelation(this.modelId, rel).subscribe(() => this.modalRef.close(true), error => this.error = error.error.message);
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
