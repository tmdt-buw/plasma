import { Component, Input, OnDestroy, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
import { FormControl } from '@angular/forms';
import { ModalOptions, NzModalService } from 'ng-zorro-antd/modal';
import { NzTabChangeEvent } from 'ng-zorro-antd/tabs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { DeltaModification, ModelingControllerService, Relation, SemanticModelNode } from '../../../api/generated/dms';
import { ModelingService } from '../../modeling.service';
import { ModalBaseConfig } from '../common/modal.base-config';
import { PlsNewEntityDialogComponent } from '../new-entity-dialog/new-entity-dialog.component';
import { PlsNewRelationDialogComponent } from '../new-relation-dialog/new-relation-dialog.component';
import { NzNotificationService } from 'ng-zorro-antd/notification';


@Component({
  selector: 'pls-concept-dialog',
  templateUrl: 'concept-dialog.component.html',
  styleUrls: ['../common/dialog.styles.scss', 'concept-dialog.component.less'],
  encapsulation: ViewEncapsulation.None
})
export class PlsConceptDialogComponent implements OnInit, OnDestroy {

  @Input() modelId: string;
  @Input() defaultNamespace: string;

  @ViewChild('colorEl') color;

  entitySearchControl = new FormControl('');
  relationSearchControl = new FormControl('');

  loadingEntityConcepts = true;
  loadingRelationConcepts = true;

  semanticModelNodes: SemanticModelNode[] = [];
  relations: Relation[] = [];


  constructor(private modeling: ModelingService, private modelingService: ModelingControllerService,
              private modal: NzModalService, private notification: NzNotificationService) {

  }

  ngOnInit(): void {
    this.entitySearchControl.valueChanges.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(() => this.getSemanticNodeTemplates());
    this.relationSearchControl.valueChanges.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(() => this.getRelationTemplates());
    this.getSemanticNodeTemplates();
    this.getRelationTemplates();
  }

  ngOnDestroy(): void {
    // remove any selected relation concept
    this.modeling.relation.next(null);
  }

  getSemanticNodeTemplates(): void {
    this.loadingEntityConcepts = true;
    this.modelingService.getElements(this.modelId, this.entitySearchControl.value).subscribe((res) => {
      this.semanticModelNodes = res;
      this.loadingEntityConcepts = false;
    });
  }

  getRelationTemplates(): void {
    this.loadingRelationConcepts = true;
    this.modelingService.getRelations(this.modelId, this.relationSearchControl.value).subscribe((res) => {
      this.relations = res;
      this.loadingRelationConcepts = false;
    }, error => this.notification.error('Error during fetch of concept and relations', 'Could not retrieve data from service. '));
  }

  onIndexChange(event: NzTabChangeEvent): void {
    if (event.index !== 1) {
      this.modeling.relation.next(null);
    }
  }

  toggleRelation(index: number, relation: Relation): void {
    if (this.isSelected(relation)) {
      this.modeling.relation.next(null);
    } else {
      this.modeling.relation.next(relation);
    }
  }

  isSelected(relation: Relation): boolean {
    const current = this.modeling.relation.getValue();
    return current === relation;
  }

  newSemanticNode(): void {
    const config: ModalOptions = {
      nzTitle: 'Add new modeling element',
      nzContent: PlsNewEntityDialogComponent,
      nzComponentParams: {
        modelId: this.modelId,
        defaultNamespace: this.defaultNamespace
      }
    };
    const ref = this.modal.create(Object.assign(config, ModalBaseConfig));
    ref.afterClose.subscribe(res => {
      if (res) {
        this.getSemanticNodeTemplates();
      }
    });
  }

  newRelationConcept(): void {
    const config: ModalOptions = {
      nzTitle: 'Add new relation',
      nzContent: PlsNewRelationDialogComponent,
      nzComponentParams: {
        modelId: this.modelId,
        defaultNamespace: this.defaultNamespace
      }
    };
    const ref = this.modal.create(Object.assign(config, ModalBaseConfig));
    ref.afterClose.subscribe(res => {
      if (res) {
        this.getRelationTemplates();
      }
    });
  }

  setRecommendation(recommendation: DeltaModification): void {
    this.modeling.recommendation.next(recommendation);
  }

  deleteProvisionalNode(e: MouseEvent, node: SemanticModelNode): void {
    this.modal.confirm({
      nzTitle: 'Remove provisional element?',
      nzContent: `Do you want to remove the provisional node '${node.uri}'?`,
      nzOnOk: () => {
        this.modelingService.deleteCachedElement(this.modelId, node.uri).subscribe(() => {
          this.getSemanticNodeTemplates();
        }, error => {
          this.notification.error('Error deleting provisional node', error.error?.message);
        });
      }
    });
  }

  deleteProvisionalRelation(e: MouseEvent, rel: Relation): void {
    this.modal.confirm({
      nzTitle: 'Remove provisional element?',
      nzContent: `Do you want to remove the provisional relation '${rel.uri}'?`,
      nzOnOk: () => {
        this.modelingService.deleteCachedRelation(this.modelId, rel.uri).subscribe(() => {
          this.getRelationTemplates();
        }, error => {
          this.notification.error('Error deleting provisional relation', error.error?.message);
        });
      }
    });
  }
}
