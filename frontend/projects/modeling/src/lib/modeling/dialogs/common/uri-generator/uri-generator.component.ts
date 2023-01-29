import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Observable } from 'rxjs';
import { FormControl, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'pls-uri-generator',
  templateUrl: './uri-generator.component.html',
  styleUrls: ['./uri-generator.component.less']
})
export class UriGeneratorComponent implements OnInit {

  generatorVisible: boolean = false;

  @Input() uri: string;
  @Input() URIUpdate: Observable<string>;
  @Input() namespaces: Array<{ prefix: string, uri: string }>;
  @Input() namespaceUpdate: Observable<string>;
  @Input() identifierUpdate: Observable<string>;
  @Input() disableAutoGeneration: boolean = false;

  @Output() URIChanged: EventEmitter<string>;

  generatorForm: FormGroup = new FormGroup({
    namespace: new FormControl(''),
    identifier: new FormControl('', [Validators.required]),
  });

  constructor() {
    this.URIChanged = new EventEmitter<string>();
  }

  ngOnInit(): void {
    this.URIUpdate?.subscribe(value => this.uri = value);
    this.namespaceUpdate?.subscribe(namespace => {
      if (!this.disableAutoGeneration && this.generatorForm.get('namespace').pristine) {
        this.generatorForm.get('namespace').setValue(namespace);
      }
    });
    this.identifierUpdate?.subscribe(identifier => {
      // console.log('new identifier', identifier, this.disableAutoGeneration, this.generatorForm.get('identifier').pristine);
      if (!this.disableAutoGeneration && this.generatorForm.get('identifier').pristine) {
        this.generatorForm.get('identifier').setValue(identifier);
      }

    });
    if (!this.uri) {
      this.uri = '';
    }
    if (this.namespaces?.length > 0) {
      this.selectNamespace(this.namespaces[0]);
    }

  }

  public getURI(): string {
    return this.uri;
  }

  showGenerator(): void {
    this.generatorVisible = true;
  }

  propagateURI(uri: string): void {
    this.URIChanged.emit(uri);
  }

  acceptURI(): void {
    const authority = this.generatorForm.get('namespace').value;
    this.uri = (!!authority ? authority : ':') + this.generatorForm.get('identifier').value;
    this.propagateURI(this.uri);
  }

  selectNamespace(namespace: { prefix: string; uri: string }): void {
    this.generatorForm.get('namespace').setValue(namespace.uri);
  }


  onURIFocusout($event: FocusEvent): void {
    console.log($event);
  }
}
