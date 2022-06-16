import { animate, style, transition, trigger } from '@angular/animations';
import { Component, ElementRef, EventEmitter, Input, OnDestroy, OnInit, Output, ViewChild } from '@angular/core';
import { FormControl } from '@angular/forms';
import { ModalOptions, NzModalRef, NzModalService } from 'ng-zorro-antd/modal';
import { BehaviorSubject, Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import {
  CombinedModel,
  DeltaModification,
  ModelingControllerService,
  Relation,
  SchemaNode,
  SemanticModelNode
} from '../api/generated/dms';
import { OntologyControllerService, OntologyInfo, SemanticModelControllerService } from '../api/generated/kgs';
import { ContextMenuService } from './context-menu/context-menu.service';
import { ModalBaseConfig, ModalMouseEnabledConfig } from './dialogs/common/modal.base-config';
import { PlsConceptDialogComponent } from './dialogs/concept-dialog/concept-dialog.component';
import {
  PlsOperationsDialogComponent
} from './dialogs/operations-dialog/operations-dialog/operations-dialog.component';
import { Filter } from './model/common/filter';
import { Nodes } from './model/configuration/node';
import { ContextMenuEvent, ContextMenuEventType } from './model/events/context-menu-event';
import { NodeChangedEvent } from './model/events/node-changed-event';
import { Util } from './model/common/util';
import { UploadOntologyComponent } from './dialogs/upload-ontology/upload-ontology.component';
import { CopySemanticModelComponent } from './dialogs/copy-semantic-model/copy-semantic-model.component';
import { NzNotificationService } from 'ng-zorro-antd/notification';
import { HttpErrorResponse, HttpStatusCode } from '@angular/common/http';


@Component({
  selector: 'pls-modeling',
  templateUrl: './modeling.component.html',
  styleUrls: ['./modeling.component.less'],
  animations: [
    // search bar animation
    trigger('search', [
      transition(':enter', [
        style({width: '0'}),
        animate('300ms', style({width: '180px'}))
      ]),
      transition(':leave', [
        animate('300ms', style({width: '0'}))
      ])
    ])
  ]
})
export class PlsModelingComponent implements OnInit, OnDestroy {

  readonly Filter = Filter;

  @Input() modelId: string;
  @Input() backgroundColor: string;
  @Input() fullScreen: boolean = true;
  @Input() viewerMode: boolean = false;
  @Input() hideFinalizeButton: boolean = false;
  @Input() hideExportButton: boolean = false;

  @ViewChild('main') main;
  @ViewChild('modelingArea') modelingArea: ElementRef;

  @Output() finished = new EventEmitter();
  @Output() clickedNode = new EventEmitter();

  // ontologies
  availableOntologies: Array<OntologyInfo>;
  selectedOntologies: Array<string>;
  defaultNamespace: string = 'local:';

  // search bar
  searchControl = new FormControl();
  isSearchExpanded = false;

  // interactions
  $center = new Subject<any>();
  $layout = new Subject<any>();
  $filter = new BehaviorSubject<Filter>(Filter.ALL);
  $URIMode = new BehaviorSubject<boolean>(false);

  exportIncludeMappings: boolean = true;

  // schema
  combinedModel: CombinedModel;
  loading: boolean = true;

  // dialogs
  private addConceptDialogRef: NzModalRef<any, any>;
  contextMenuSubscription;
  private uploadOntologyDialogRef: NzModalRef<any, any>;
  private copySemanticModelDialogRef: NzModalRef<any, any>;

  // tools
  recommendationsEnabled = true;

  get isFullscreen(): boolean {
    return !!this.main?.fullScreenIsActive;
  }

  constructor(private modelingService: ModelingControllerService, private contextMenu: ContextMenuService, private ontologyService: OntologyControllerService,
              private semanticModelService: SemanticModelControllerService, private modal: NzModalService, private notification: NzNotificationService) {
  }

  ngOnInit(): void {
    this.ontologyService.listOntologies().subscribe((ontologies: Array<OntologyInfo>) => {
      this.availableOntologies = ontologies;
      this.defaultNamespace = ontologies.find(ont => ont.local)?.prefix;
      if (!this.defaultNamespace) {
        this.defaultNamespace = '';
      }
    });
    this.modelingService.getSelectedOntologies(this.modelId).subscribe((ontologies: Array<string>) => {
      this.selectedOntologies = ontologies;
    });

    // query schema
    this.getSchema();
    // subscribe to search
    this.searchControl.valueChanges.pipe(
      distinctUntilChanged(),
      debounceTime(1000)
    ).subscribe((value) => {
      // TODO
    });
    this.contextMenuSubscription = this.contextMenu.onClosed.subscribe((event: ContextMenuEvent) => {
      if (event) {
        if (event.type === ContextMenuEventType.updateEntity) {
          const delta: DeltaModification = {
            entities: [event.target]
          };
          this.modelingService.modifyModel(this.modelId, delta).subscribe((res: CombinedModel) => this.combinedModel = res);
        } else if (event.type === ContextMenuEventType.removeEntity) {
          const delta: DeltaModification = {
            entities: [event.target],
            deletion: true
          };
          this.modelingService.modifyModel(this.modelId, delta).subscribe((res: CombinedModel) => this.combinedModel = res);
        } else if (event.type === ContextMenuEventType.removeRelation) {
          const delta: DeltaModification = {
            relations: [event.target],
            deletion: true
          };
          this.modelingService.modifyModel(this.modelId, delta).subscribe((res: CombinedModel) => this.combinedModel = res);
        } else if (event.type === ContextMenuEventType.performOperation) {
          const operation = event.operation;
          const config: ModalOptions = {
            nzTitle: operation.label,
            nzContent: PlsOperationsDialogComponent,
            nzComponentParams: {
              operation
            }
          };
          const dialogRef = this.modal.create(Object.assign(config, ModalBaseConfig));
          dialogRef.afterClose.subscribe(context => {
            if (context) {
              this.modelingService.modifySyntacticSchema(this.modelId, context).subscribe((res: CombinedModel) => this.combinedModel = res);
            }
          });
        }
      }
    });
  }

  ngOnDestroy(): void {
    this.modal.closeAll();
    this.contextMenuSubscription.unsubscribe();
    this.addConceptDialogRef?.close();
    this.uploadOntologyDialogRef?.close();
  }

  getSchema(): void {
    this.modelingService.getCombinedModel(this.modelId).subscribe((schema: CombinedModel) => {
        this.combinedModel = schema;
        if (this.viewerMode) {
          this.$filter.next(Filter.SEMANTIC);
        }
        this.loading = false;
      },
      (error: HttpErrorResponse) => {
        if (error.status === HttpStatusCode.ServiceUnavailable) {
          this.notification.error('Service not available', 'The modeling service is currently not available, please try again later');
        } else if (error.status === HttpStatusCode.NotFound) {
          this.notification.error('Model not found', 'The requested model could not be found');
        } else {
          this.notification.error('Error retrieving model', 'An unexpected error occurred.');
        }
      }
    );
  }

  // ======= Menu functions ===========

  undo(): void {
    if (this.isFinalized()) {
      return;
    }
    this.modelingService.undo(this.modelId).subscribe((res: CombinedModel) => this.combinedModel = res);
  }

  redo(): void {
    if (this.isFinalized()) {
      return;
    }
    this.modelingService.redo(this.modelId).subscribe((res: CombinedModel) => this.combinedModel = res);
  }

  onNodeClicked(event: string): void {
    this.clickedNode.emit(event);
  }

  onNodeChanged(event: { entities?: Array<SemanticModelNode>, nodes?: Array<SchemaNode> }): void {
    const delta: DeltaModification = {
      nodes: event.nodes,
      entities: event.entities
    };
    this.modelingService.modifyModel(this.modelId, delta).subscribe((res) => this.combinedModel = res);
  }

  onSemanticElementsAdded(event: { entities?: Array<SemanticModelNode>, relations?: Array<Relation> }): void {
    const delta: DeltaModification = {
      relations: event.relations,
      entities: event.entities
    };
    this.modelingService.modifyModel(this.modelId, delta).subscribe((res) => this.combinedModel = res);
  }

  onSemanticElementsRemoved(event: { entities?: Array<SemanticModelNode>, relations?: Array<Relation> }): void {
    const delta: DeltaModification = {
      relations: event.relations,
      entities: event.entities,
      deletion: true
    };
    this.modelingService.modifyModel(this.modelId, delta).subscribe((res) => this.combinedModel = res);
  }

  onGraphLaidOut(event: CombinedModel): void {
    if (this.combinedModel?.finalized) {
      return;
    }
    this.modelingService.updatePositions(this.modelId, event.semanticModel.nodes.concat(event.syntaxModel.nodes)).subscribe();
  }

  onNodePositionChanged(event: NodeChangedEvent): void {
    if (this.combinedModel?.finalized) {
      return;
    }
    switch (event.type) {
      case Nodes.syntaxNodeName:
        const delta: DeltaModification = {
          nodes: [event.node]
        };
        this.modelingService.modifyModel(this.modelId, delta).subscribe();
        break;
      case Nodes.semanticClassName:
      case Nodes.extendedSemanticClassName:
        const delta2: DeltaModification = {
          entities: [event.node]
        };
        this.modelingService.modifyModel(this.modelId, delta2).subscribe();
        break;
      default:
        console.error('Unsupported node type.');
    }
  }

  toggleURIMode(checked: boolean): void {
    this.$URIMode.next(checked);
  }

  /**
   * toggle search bar visibility
   */
  toggleSearch(): void {
    this.isSearchExpanded = !this.isSearchExpanded;
  }

  onFilterChange(event): void {
    this.$filter.next(event);
  }

  onRecommendationAccepted(event: DeltaModification): void {
    console.log('accepting recommendation', event);
    this.modelingService.acceptRecommendation(this.modelId, event).subscribe(cm =>
      this.combinedModel = cm
    );
  }

  finalizeModel(): void {
    // close all dialogs
    this.modal.closeAll();
    this.viewerMode = true;
    this.modelingService.finalizeModeling(this.modelId).subscribe(() => {
      this.finished.emit();
    }, () => {
      this.viewerMode = false;
      this.notification.error('Could not finalize model', 'An error occurred during model finalization. Please contact an admin for more information.');
    });
  }

  exportModel(): void {
    if (this.combinedModel.semanticModel.id) {
      this.semanticModelService.exportStoredSemanticModel(this.combinedModel.semanticModel.id, 'TURTLE')
        .subscribe((res) => Util.download('model.ttl', res),
          () => this.notification.error('Could not export model', 'An error occurred during model export. Please try again in a few seconds.')
        );
    } else {
      this.semanticModelService.convertSemanticModel(this.combinedModel, 'TURTLE', this.exportIncludeMappings)
        .subscribe((res) => Util.download('model.ttl', res), () => this.notification.error('Could not export model', 'An error occurred during model export. Please try again in a few seconds.'));
    }
  }

  isFinalized(): boolean {
    return this.combinedModel?.finalized;
  }

  // DIALOGS
  showAddElementsDialog(): void {
    if (this.isFinalized()) {
      return;
    }
    if (this.addConceptDialogRef) {
      this.addConceptDialogRef.close();
      this.addConceptDialogRef = undefined;
      return;
    }
    this.uploadOntologyDialogRef?.close();
    const rect: DOMRect = this.modelingArea.nativeElement.getBoundingClientRect();
    const config: ModalOptions = {
      nzStyle: {position: 'absolute', top: `${rect.top + 15}px`, left: `10px`, 'max-width': '30%', bottom: '15px'},
      nzClassName: 'pls-concept-dialog',
      nzTitle: '',
      nzContent: PlsConceptDialogComponent,
      nzComponentParams: {
        modelId: this.modelId,
        defaultNamespace: this.defaultNamespace
      }
    };
    this.addConceptDialogRef = this.modal.create(Object.assign(config, ModalMouseEnabledConfig));
    this.addConceptDialogRef.afterClose.subscribe(() => this.addConceptDialogRef = undefined);
  }


  closeAddConceptDialog(): void {
    if (this.addConceptDialogRef) {
      this.addConceptDialogRef.close();
      this.addConceptDialogRef = undefined;
    }
  }

  showUploadOntologyDialog(): void {
    if (this.uploadOntologyDialogRef) {
      this.uploadOntologyDialogRef.close();
      this.uploadOntologyDialogRef = undefined;
      return;
    }
    this.closeAddConceptDialog();
    const rect: DOMRect = this.modelingArea.nativeElement.getBoundingClientRect();
    const config: ModalOptions = {
      nzStyle: {position: 'absolute', top: `${rect.top + 15}px`, right: `10px`, 'max-width': '30%', bottom: '15px'},
      nzClassName: 'pls-upload-ontology',
      nzTitle: 'Add ontology',
      nzContent: UploadOntologyComponent,
      nzComponentParams: {}
    };
    this.uploadOntologyDialogRef = this.modal.create(Object.assign(config, ModalMouseEnabledConfig));
    this.uploadOntologyDialogRef.afterClose.subscribe(() => this.uploadOntologyDialogRef = undefined);
    this.uploadOntologyDialogRef.getContentComponent().ontologyCreated.subscribe(() => {
      this.ontologyService.listOntologies().subscribe((ontologies: Array<OntologyInfo>) => {
        this.availableOntologies = ontologies;
      });
    });
  }

  onOntologySelectionChanged(list: Array<string>): void {
    this.closeAddConceptDialog();
    this.modelingService.updateSelectedOntologies(this.modelId, list).subscribe();
    // if (this.reopenConceptsDialog) {
    //   this.showAddConceptDialog();
    // }
  }

  openSemanticModelCopyDialog(): void {
    this.closeAddConceptDialog();
    const rect: DOMRect = this.modelingArea.nativeElement.getBoundingClientRect();
    const config: ModalOptions = {
      nzStyle: {position: 'absolute', top: `${rect.top + 15}px`, left: `15%`, width: '70%', bottom: '15px'},
      nzClassName: 'pls-copy-semantic-model',
      nzTitle: 'Import Semantic Model from other Model',
      nzContent: CopySemanticModelComponent,
      nzComponentParams: {
        currentModelId: this.modelId
      }
    };
    this.copySemanticModelDialogRef = this.modal.create(Object.assign(config, ModalMouseEnabledConfig));
    this.copySemanticModelDialogRef.afterClose.subscribe(() => {
      this.copySemanticModelDialogRef = undefined;
    });
    this.copySemanticModelDialogRef.getContentComponent().copySuccessful.subscribe(() => {
      this.closeSemanticModelCopyDialog();
      this.getSchema();
    });
  }

  closeSemanticModelCopyDialog(): void {
    this.copySemanticModelDialogRef?.close();
  }

  performSemanticLabeling(): void {
    if (!this.recommendationsEnabled) {
      return;
    }
    this.modelingService.performSemanticLabeling(this.modelId).subscribe(combinedModel => {
      this.combinedModel = combinedModel;
      this.notification.success('Semantic Labeling', 'Requested a semantic labeling. The result has been applied.');
    }, error => {
      this.notification.error('Could not perform semantic labeling', error.error.message);
    });
  }

  performSemanticModeling(): void {
    if (!this.recommendationsEnabled) {
      return;
    }
    this.modelingService.performSemanticModeling(this.modelId).subscribe(combinedModel => {
      this.combinedModel = combinedModel;
      this.notification.success('Semantic Modeling', 'Requested a semantic modeling. The result has been applied.');
    }, error => {
      console.log(error);
      this.notification.error('Could not perform semantic modeling', error.error.message);
    });
  }

  isViewerMode(): boolean {
    return this.viewerMode || this.combinedModel?.finalized;
  }
}
