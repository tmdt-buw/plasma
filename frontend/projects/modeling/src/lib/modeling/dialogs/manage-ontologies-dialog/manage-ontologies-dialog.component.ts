import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { ModalOptions, NzModalRef, NzModalService } from 'ng-zorro-antd/modal';
import { UploadOntologyComponent } from '../upload-ontology/upload-ontology.component';
import { ModalMouseEnabledConfig } from '../common/modal.base-config';
import { OntologyControllerService, OntologyInfo, SemanticModelControllerService } from '../../../api/generated/kgs';
import { ModelingControllerService } from '../../../api/generated/dms';
import { ContextMenuService } from '../../context-menu/context-menu.service';
import { NzNotificationService } from 'ng-zorro-antd/notification';

@Component({
  selector: 'pls-manage-ontologies-dialog',
  templateUrl: './manage-ontologies-dialog.component.html',
  styleUrls: ['./manage-ontologies-dialog.component.css']
})
export class ManageOntologiesDialogComponent implements OnInit, OnDestroy{

  private uploadOntologyDialogRef: NzModalRef<any, any>;
  availableOntologies: Array<OntologyInfo>;
  selected: OntologyInfo;

  @Input() boundingBox: DOMRect;

  @Output() ontologyDeleted = new EventEmitter();
  @Output() ontologyCreated = new EventEmitter();

  constructor(private modelingService: ModelingControllerService, private contextMenu: ContextMenuService, private ontologyService: OntologyControllerService,
              private semanticModelService: SemanticModelControllerService, private modal: NzModalService, private notification: NzNotificationService) {
  }

  ngOnInit(): void {
    this.ontologyService.listOntologies().subscribe((ontologies: Array<OntologyInfo>) => {
      this.availableOntologies = ontologies;
    });
  }

  ngOnDestroy():void {
    this.uploadOntologyDialogRef?.close();
  }


  showUploadOntologiesDialog(): void {
    if (this.uploadOntologyDialogRef) {
      this.uploadOntologyDialogRef.close();
      this.uploadOntologyDialogRef = undefined;
      return;
    }

    const config: ModalOptions = {
      nzStyle: {
        position: 'absolute',
        top: `${this.boundingBox.top + 15}px`,
        right: `10px`,
        'max-width': '30%',
        bottom: '15px'
      },
      nzClassName: 'pls-upload-ontology',
      nzTitle: 'Add ontology',
      nzContent: UploadOntologyComponent,
      nzComponentParams: {}
    };
    this.uploadOntologyDialogRef = this.modal.create(Object.assign(config, ModalMouseEnabledConfig));
    this.uploadOntologyDialogRef.afterClose.subscribe(() => this.uploadOntologyDialogRef = undefined);
    this.uploadOntologyDialogRef.getContentComponent().ontologyCreated.subscribe(() => {
      this.refreshOntologies();
      this.ontologyCreated.emit();
    });
  }

  private refreshOntologies(): void {
    this.ontologyService.listOntologies().subscribe((ontologies: Array<OntologyInfo>) => {
      this.availableOntologies = ontologies;
    });
  }


  selectOntology(ont: OntologyInfo): void {
    this.selected = ont;
  }

  deleteOntology(selected: OntologyInfo): void {
    this.ontologyService.deleteOntology(selected.label).subscribe(() => {
      this.notification.success('Ontology deleted', `Ontology ${selected.label} successfully deleted`);
      this.ontologyDeleted.emit(selected);
      const index = this.availableOntologies.indexOf(selected, 0);
      if (index > -1) {
        this.availableOntologies.splice(index, 1);
      }
      this.selected = undefined;
      this.refreshOntologies();
    }, error => {
      this.notification.error('Error during ontology delete', `Ontology ${selected.label} could not be deleted: ${error.message}`);
    });
  }

  isSelected(ont: OntologyInfo): boolean {
    return this.selected?.label === ont.label;
  }
}
