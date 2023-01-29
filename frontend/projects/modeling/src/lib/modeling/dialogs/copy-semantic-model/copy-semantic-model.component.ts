import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CombinedModel, ModelingControllerService } from '../../../api/generated/dms';
import { BehaviorSubject, Subject } from 'rxjs';
import { Filter } from '../../model/common/filter';
import { NzNotificationService } from 'ng-zorro-antd/notification';

@Component({
  selector: 'pls-copy-semantic-model',
  templateUrl: './copy-semantic-model.component.html',
  styleUrls: ['./copy-semantic-model.component.less']
})
export class CopySemanticModelComponent implements OnInit {

  @Input() currentModelId: string;

  sourceModelId: string;
  combinedModel: CombinedModel;
  loading: boolean;
  availableModels: Array<{}>;

  $filter = new BehaviorSubject<Filter>(Filter.SEMANTIC);
  $URIMode = new BehaviorSubject<boolean>(true);
  $center = new Subject<any>();
  $layout = new Subject<any>();

  @Output() copySuccessful = new EventEmitter();


  constructor(private modelingService: ModelingControllerService, private notification: NzNotificationService) {
  }

  ngOnInit(): void {
    this.getAvailableModels();
  }

  onModelSelectionChanged($event: string): void {
    this.sourceModelId = $event;
    this.loading = true;
    this.getModel();
  }

  getAvailableModels(): void {
    this.modelingService.listModelings().subscribe(
      modelings => {
        this.availableModels = modelings
          .filter(modeling => modeling.id !== this.currentModelId)
          .filter(modeling => modeling.finalized);
      });
  }

  getModel(): void {
    this.modelingService.getCombinedModel(this.sourceModelId).subscribe((schema: CombinedModel) => {
        this.combinedModel = schema;
        this.loading = false;
        this.$filter.next(Filter.SEMANTIC);
        this.$center.next();
      },
      error => {
        this.notification.create(
          'error',
          'No model',
          'The selected model is not available.'
        );
        this.combinedModel = undefined;
        this.loading = false;
      });
  }

  onModelCopyClick(): void {
    this.modelingService.copySemanticModelFromOtherModel(this.currentModelId, this.sourceModelId).subscribe((model) => {
      this.notification.create(
        'success',
        'Import Successful',
        'The model has been successfully imported.'
      );
      this.copySuccessful.next(model);
    }, () => {
      this.notification.create(
        'error',
        'Import Failed',
        'The model could not be imported.'
      );
    });
  }
}
