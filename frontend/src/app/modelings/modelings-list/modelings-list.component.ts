import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { Router } from '@angular/router';
import { ModelingControllerService, ModelingInfo } from '@tmdt/dms';
import { DatePipe } from '@angular/common';
import { ModalOptions, NzModalRef, NzModalService } from 'ng-zorro-antd/modal';
import { appRoutesNames } from '../../app.routes.names';
import { NzNotificationService } from 'ng-zorro-antd/notification';
import { HttpStatusCode } from '@angular/common/http';
import { DataProcessingControllerService } from '../../dataprocessing/api/generated';
import {
  ModalBaseConfig,
  ModalMouseEnabledConfig
} from '../../../../projects/modeling/src/lib/modeling/dialogs/common/modal.base-config';
import { ConversionDialogComponent } from '../../dataprocessing/conversion-dialog/conversion-dialog.component';
import { OntologyControllerService } from '../../../../projects/modeling/src/lib/api/generated/kgs';
import { Util } from 'projects/modeling/src/lib/modeling/model/common/util';
import { EditModelingComponent } from '../edit-modeling/edit-modeling.component';

@Component({
  selector: 'app-modelings-list',
  templateUrl: './modelings-list.component.html',
  styleUrls: ['./modelings-list.component.less'],
  providers: [DatePipe]
})
export class ModelingsListComponent implements OnInit {

  modelings: ModelingInfo[] = [];
  isLoading = false;
  error = null;
  modelsRoute: string = appRoutesNames.MODELS;

  // output to emit created event
  @Output() modelingSelected = new EventEmitter<ModelingInfo>();
  dpsAvailable: boolean;
  private editModelingModalRef: NzModalRef<any, any>;

  constructor(private modelingController: ModelingControllerService, private router: Router, private date: DatePipe,
              private modal: NzModalService, private notification: NzNotificationService, private processingService: DataProcessingControllerService, private ontologyService: OntologyControllerService) {
  }

  /**
   * Perform actions to init view
   */
  ngOnInit(): void {
    this.getModelings();
    this.processingService.isAvailable().subscribe(() => this.dpsAvailable = true,
      () => this.dpsAvailable = false);
  }

  /**
   * Get available modelings from backend
   */
  getModelings(): void {
    this.isLoading = true;
    this.error = null;
    this.modelingController.listModelings().subscribe(
      (modelings) => {
        this.error = null;
        this.modelings = modelings;
        this.isLoading = false;
      }, error => {
        if (error.status === 0) {
          this.notification.error('Connection error occurred', 'This is most likely caused by a CORS error. Check the system setup.');
        } else if (error.status === HttpStatusCode.ServiceUnavailable) {
          this.notification.error('Modeling Service Not Available', 'The modeling service could not be reached. Please try again in a few minutes or contact support.');
        } else {
          this.notification.error('Unexpected error occurred', 'The modeling service returned:<br>' + error.statusText + ': ' + error.message);
        }
        this.error = error;
        this.isLoading = false;
      });
  }

  /**
   * Delete a modeling
   * @param modeling to delete
   */
  deleteModeling(modeling: ModelingInfo): void {
    this.isLoading = true;
    this.error = null;
    this.modelingController.deleteModel(modeling.id).subscribe(() => {
        this.modelings = this.modelings.filter(d => d.id !== modeling.id);
        this.isLoading = false;
      }, error => {
        this.error = error;
        this.isLoading = false;
      }
    );
  }

  showUpdateModelingModal(info: ModelingInfo): void {
    // const rect: DOMRect = this.modelingArea.nativeElement.getBoundingClientRect();
    const config: ModalOptions = {
      nzStyle: {position: 'absolute', top: `20%`, left: `10%`, width: '80%', bottom: '20%'},
      nzClassName: 'pls-edit-modeling',
      nzTitle: 'Edit modeling',
      nzContent: EditModelingComponent,
      nzComponentParams: {
        modeling: info
      }
    };
    this.editModelingModalRef = this.modal.create(Object.assign(config, ModalMouseEnabledConfig));
    this.editModelingModalRef.afterClose.subscribe(result => {
      if (result) {
        this.modelingController.updateModeling(info.id, result.name, result.description).subscribe(update => {
            info.name = update.name;
            info.description = update.description;
            this.notification.success('Modeling updated', 'Modeling information updated');
          },
          err => this.notification.error('Modeling update failed', 'Could not update modeling: ' + err.message));
      }
      this.editModelingModalRef = undefined;
    });
    // this.modal.create({
    //   nzTitle: title,
    //   nzContent: description,
    //   nzWidth: '70%',
    //   nzClosable: false,
    //   nzCancelText: null,
    //   nzOkText: 'Close'
    // });
  }

  openConversionModal(model: ModelingInfo): void {
    const config: ModalOptions = {
      nzTitle: 'Convert ' + model.name,
      nzStyle: {position: 'absolute', top: `20%`, left: `25%`, width: '50%', bottom: '20%'},
      nzContent: ConversionDialogComponent,
      nzComponentParams: {
        model
      }
    };
    const ref = this.modal.create(Object.assign(config, ModalBaseConfig));
  }

  downloadLocalOntology(): void {
    this.ontologyService.downloadLocalOntology('TURTLE', 'response').subscribe(res => {
      const filename = res.headers.get('filename');
      Util.download(filename ? filename : 'local_ontology.ttl', res.body);
    }, err => this.notification.error('Download failed', 'Cannot download the local ontology: ' + err.message));
  }

  cloneModeling(modelingId: string): void {
    console.log(this.modelings);
    this.modelingController.cloneModeling(modelingId).subscribe(mi => {
        this.modelings = [mi, ...this.modelings];
        console.log(this.modelings);
        this.notification.success('Cloning finished', 'New modeling has been added to the list');
      },
      err => this.notification.error('Cloning failed', 'Cannot clone this model: ' + err.message));

  }
}
