import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { NzModalRef } from 'ng-zorro-antd/modal';
import { Class, ModelingControllerService, NamedEntity } from '../../../api/generated/dms';
import { UriGeneratorComponent } from '../common/uri-generator/uri-generator.component';
import { BehaviorSubject, Subject } from 'rxjs';
import { OntologyControllerService } from '../../../api/generated/kgs';

@Component({
  selector: 'pls-edit-entity-dialog',
  templateUrl: './edit-entity-dialog.component.html',
  styleUrls: ['../common/dialog.styles.scss']
})
export class PlsEditEntityDialogComponent implements OnInit {

  @Input() modelId: string;
  @Input() namespaces: Array<{ prefix: string, uri: string }>;
  @Input() editNode: Class | NamedEntity;

  namespace: Subject<string>;
  identifier: Subject<string>;
  uriCheckStatus = undefined;
  uriError: string;
  uriWarning: boolean;

  @ViewChild('uri_generator') uriGenerator: UriGeneratorComponent;

  form = new FormGroup({
    label: new FormControl('', [Validators.required]),
    description: new FormControl('', [Validators.required]),
    uri: new FormControl('', [Validators.required]),
    namedEntity: new FormControl(false)
  });

  error;

  constructor(private modalRef: NzModalRef, private modelingService: ModelingControllerService, private ontologyService: OntologyControllerService) {
  }

  ngOnInit(): void {
    if (this.editNode) {
      this.form.get('label').setValue(this.editNode.label);
      this.form.get('description').setValue(this.editNode.description);
      this.form.get('uri').setValue(this.editNode.uri);
      this.form.get('namedEntity').setValue(this.editNode._class === 'NamedEntity');
      this.uriWarning = true;
    }

    this.identifier = new BehaviorSubject<string>('');
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
        uri: this.uriGenerator.getURI(),
        description: this.form.get('description').value,
        label: this.form.get('label').value
      };
    } else {
      node = {
        _class: 'Class',
        uri: this.uriGenerator.getURI(),
        description: this.form.get('description').value,
        label: this.form.get('label').value
      };
    }
    if (this.editNode) {
      // this is an edit, not a new node
      this.modelingService.updateProvisionalNode(this.modelId, node, this.editNode.uri === node.uri ? undefined : this.editNode.uri).subscribe(() => this.modalRef.close(true), error => this.error = error.error.message);
    } else {
      this.modelingService.cacheElement(this.modelId, node).subscribe(() => this.modalRef.close(true), error => this.error = error.error.message);
    }
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

  onLabelEdited(event: Event): void {
    const identifier = this.generateIdentifier();
    this.identifier.next(identifier);
  }

  updateURI(uri: string): void {
    this.uriCheckStatus = 'validating';
    this.ontologyService.validateURI(uri).subscribe(() => {
      this.form.get('uri').setValue(uri);
      this.uriCheckStatus = 'success';
      this.uriError = undefined;
      this.form.updateValueAndValidity();
    }, error => {
      this.form.get('uri').setValue(uri);
      this.uriCheckStatus = 'error';
      this.uriError = error.error.message;
      this.form.setErrors({invalid: true});
    });
  }

  getNamedEntityTooltip(): string {
    return !!this.editNode ? 'Cannot be changed for an existing element.' : undefined;
  }
}
