import { Component, ElementRef, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { OntologyControllerService } from '../../../api/generated/kgs';

@Component({
  selector: 'pls-upload-ontology',
  templateUrl: './upload-ontology.component.html',
  styleUrls: ['./upload-ontology.component.css']
})
export class UploadOntologyComponent {

  readonly multipartName = 'file';

  @Input() supportedFileFormats: string[];

// form
  form = new FormGroup({
    label: new FormControl('', [Validators.required, Validators.maxLength(6), Validators.minLength(2)]),
    uri: new FormControl('', [Validators.required]),
    prefix: new FormControl('', [Validators.required, Validators.maxLength(6), Validators.minLength(2)]),
    description: new FormControl('', [])
  });
  error;
  success;

  ontologyFiles: File[] = [];

  @Output() ontologyCreated = new EventEmitter();

  @ViewChild('fileInput') fileInput: ElementRef;

  constructor(private ontologyController: OntologyControllerService) {
  }

  /**
   * Submits the ontology.
   */
  onSubmit(): void {
    this.error = null;
    this.ontologyController.addOntology(
      this.form.get('label').value,
      this.form.get('prefix').value,
      this.form.get('uri').value,
      this.ontologyFiles[0]
    ).subscribe(() => {
      this.form.reset();
      this.ontologyFiles = [];
      this.success = 'Ontology added.';
      this.error = undefined;
      this.ontologyCreated.next();
    }, error => {
      this.success = undefined;
      this.error = error;
    });
  }

  beforeUpload = (file: File): boolean => {
    if (this.checkFileExtension(file)) {
      this.ontologyFiles = [file];
    } else {
      this.ontologyFiles = [];
    }
    return false;
  }

  /**
   * Checks if the form is valid
   */
  isValid(): boolean {
    return this.form.valid && this.ontologyFiles?.length === 1;
  }

  /**
   * checks the fileextension
   */
  checkFileExtension(file: File): boolean {
    const fileExtension = file.name.split('.').pop().toLowerCase();
    if (!this.supportedFileFormats || this.supportedFileFormats.includes(fileExtension)) {
      return true;
    } else {
      console.error('Unsupported file format');
      return false;
    }
  }
}

