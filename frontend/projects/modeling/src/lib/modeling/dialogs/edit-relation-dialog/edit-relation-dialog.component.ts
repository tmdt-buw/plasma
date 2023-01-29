import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { NzModalRef } from 'ng-zorro-antd/modal';
import { ModelingControllerService, ObjectProperty, Relation } from '../../../api/generated/dms';
import { BehaviorSubject, Subject } from 'rxjs';
import { UriGeneratorComponent } from '../common/uri-generator/uri-generator.component';
import { OntologyControllerService } from '../../../api/generated/kgs';

@Component({
  selector: 'pls-edit-relation-dialog',
  templateUrl: './edit-relation-dialog.component.html',
  styleUrls: ['../common/dialog.styles.scss']
})
export class PlsEditRelationDialogComponent implements OnInit {

  @Input() modelId: string;
  @Input() namespaces: Array<{ prefix: string, uri: string }>;
  @Input() editRelation: Relation;

  identifier: Subject<string>;
  uriCheckStatus = undefined;
  uriError: string;
  uriWarning: boolean;

  @ViewChild('uri_generator') uriGenerator: UriGeneratorComponent;

  form = new FormGroup({
    label: new FormControl('', [Validators.required]),
    description: new FormControl('', [Validators.required]),
    uri: new FormControl('', [Validators.required]),
    type: new FormControl('', [Validators.required])
  });

  error;
  relationType: 'ObjectProperty' | 'DataProperty';
  existingURI: string;

  constructor(private modalRef: NzModalRef, private modelingService: ModelingControllerService, private ontologyService: OntologyControllerService) {
  }

  ngOnInit(): void {
    if (this.editRelation) {
      this.existingURI = this.editRelation.uri;
      this.form.get('label').setValue(this.editRelation.label);
      this.form.get('description').setValue(this.editRelation.description);
      this.form.get('uri').setValue(this.editRelation.uri);
      this.relationType = (this.editRelation._class === 'ObjectProperty') ? 'ObjectProperty' : 'DataProperty';
      this.uriWarning = true;
    }
    this.identifier = new BehaviorSubject<string>('');
  }

  submit(): void {
    this.error = null;
    const rel: Relation = {
      _class: this.form.get('type').value,
      uri: this.uriGenerator.getURI(),
      label: this.form.get('label').value,
      description: this.form.get('description').value
    };
    if (this.editRelation) {
      // this is an edit, not a new node
      this.modelingService.updateProvisionalRelation(this.modelId, rel, this.editRelation.uri === rel.uri ? undefined : this.editRelation.uri)
        .subscribe(() => this.modalRef.close(true), error => this.error = error.error.message);
    } else {
      this.modelingService.cacheRelation(this.modelId, rel).subscribe(() => this.modalRef.close(true), error => this.error = error.error.message);
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


  getRelationTypeTooltip(): string {
    return !!this.editRelation ? 'Cannot be changed for an existing relation.' : undefined;
  }


  updateURI(uri: string): void {
    this.uriCheckStatus = 'validating';
    this.ontologyService.validateURI(uri).subscribe(() => {
      this.form.get('uri').setValue(uri);
      this.uriError = undefined;
      this.uriCheckStatus = 'success';
      this.form.updateValueAndValidity();
    }, error => {
      this.form.get('uri').setValue(uri);
      this.uriCheckStatus = 'error';
      this.uriError = error.error.message;
      this.form.setErrors({invalid: true});
    });
  }
}
