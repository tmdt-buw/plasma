import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import {
  Class,
  DeltaModification,
  Instance,
  Literal,
  ModelingControllerService,
  NamedEntity,
  SemanticModelNode
} from '../../../api/generated/dms';
import { EditState } from './model/edit-state';
import { FormBuilder, Validators } from '@angular/forms';

export interface PlsSemanticNodeDetailsData {
  modelId: string;
  node: SemanticModelNode;
  recommendations: DeltaModification[];
  examples: string[];
  finalized: boolean;
}

@Component({
  selector: 'pls-semantic-node-details',
  templateUrl: 'semantic-node.component.html',
  styleUrls: ['../common/dialog.styles.scss', 'semantic-node.component.less']
})
export class PlsSemanticNodeDetailsComponent implements OnInit {

  @Input() node: PlsSemanticNodeDetailsData;

  $edit: BehaviorSubject<EditState> = new BehaviorSubject<EditState>(EditState.confirm);
  label: string;
  description: string;

  form;

  @Output()
  public recommendationSelect: EventEmitter<DeltaModification> = new EventEmitter();
  @Output()
  public recommendationAccept: EventEmitter<DeltaModification> = new EventEmitter();
  @Output()
  public recommendationReject: EventEmitter<DeltaModification> = new EventEmitter();
  @Output()
  public nodeEdited: EventEmitter<SemanticModelNode> = new EventEmitter<SemanticModelNode>();


  constructor(private modelingService: ModelingControllerService, private fb: FormBuilder) {
  }

  ngOnInit(): void {
    if (this.isClass()) {
      this.form = this.fb.group({
        label: [{
          value: this.getInstance()?.label,
          disabled: !this.editing
        }, [Validators.required, Validators.maxLength(40)]],
        description: [{
          value: this.getInstance()?.description,
          disabled: !this.editing
        }, [Validators.required, Validators.maxLength(200)]]
      });
    } else if (this.isLiteral()) {
      this.form = this.fb.group({
        value: [{
          value: (this.node.node as Literal).label,
          disabled: !this.editing
        }, Validators.required]
      });
    }

    this.$edit.subscribe((editState: EditState) => {
      switch (editState) {
        case EditState.start:
          this.form?.enable();
          break;
        case EditState.confirm:
        case EditState.cancel:
          this.form?.disable();
      }
    });
  }

  startEdit(): void {
    switch (this.node.node._class) {
      case 'Class':
        const instance = this.getInstance();
        if (!instance) {
          return;
        }
        this.$edit.next(EditState.start);
        this.label = instance.label;
        this.description = instance.description;
        break;
      case 'NamedEntity':
        break;
      case 'Literal':
        this.$edit.next(EditState.start);
        this.label = this.node.node.label;
        break;
    }
  }

  acceptEdit(): void {
    this.$edit.next(EditState.confirm);
    if (this.isClass()) {
      const clazz = this.node.node as Class;
      clazz.instance.label = this.form.controls.label.value;
      clazz.instance.description = this.form.controls.description.value;
    } else if (this.isLiteral()) {
      const literal = this.node.node as Literal;
      literal.label = this.form.controls.value.value;
    }

    const delta: DeltaModification = {
      entities: [this.node.node]
    };

    this.modelingService.modifyModel(this.node.modelId, delta).subscribe(() => {
      this.nodeEdited.next(this.node.node);
    }, () => {
      switch (this.node.node._class) {
        case 'Class':
          this.getInstance().label = this.label;
          this.getInstance().description = this.description;
          break;
        case 'Literal':
          const literal: Literal = this.node.node as Literal;
          literal.label = this.label;
      }
    });
  }

  cancelEdit(): void {
    this.$edit.next(EditState.cancel);
  }

  get editing(): boolean {
    return this.$edit.getValue() === EditState.start;
  }

  showRecommendation(recommendation: DeltaModification): void {
    console.log('emitting select for recommendation', recommendation);
    this.recommendationSelect.emit(recommendation);
  }

  hideRecommendation(): void {
    this.recommendationSelect.emit(null);
  }

  acceptRecommendation(recommendation: DeltaModification): void {
    this.recommendationAccept.emit(recommendation);
  }

  rejectRecommendation(recommendation: DeltaModification): void {
    // nyi
  }

  getDescription(): string {
    switch (this.node.node._class) {
      case 'Class':
        return (this.node.node as Class).description;
      case 'NamedEntity':
        return (this.node.node as NamedEntity).description;
    }
    return 'No description available';
  }

  isNamedEntity(): boolean {
    return this.node.node._class === 'NamedEntity';
  }

  isClass(): boolean {
    return this.node.node._class === 'Class';
  }

  isLiteral(): boolean {
    return this.node.node._class === 'Literal';
  }

  isMappedNode(): boolean {
    if (this.node.node._class === 'Class') {
      const clazz = this.node.node as Class;
      return !!clazz.syntaxNodeUuid;
    } else if (this.node.node._class === 'Literal') {
      const literal = this.node.node as Literal;
      return !!literal.syntaxNodeUuid;
    }
    return false;
  }

  hasExampleValues(): boolean {
    // console.log('examples');
    return this.node.examples?.length > 0;
  }

  getInstance(): Instance {
    if (this.node.node._class !== 'Class') {
      return;
    }
    const clazz = this.node.node as Class;
    if (!clazz.instance) {
      return;
    }
    return clazz.instance;
  }

  getAsMappedNode(): Literal | Class {
    if (this.isClass()) {
      return this.node.node as Class;
    }
    if (this.isLiteral()) {
      return this.node.node as Literal;
    }
  }

  getClassOrNamedEntity(): Class | NamedEntity {
    if (this.isClass()) {
      return this.node.node as Class;
    }
    if (this.isNamedEntity()) {
      return this.node.node as NamedEntity;
    }
  }

  customize(): void {
    if (!this.isClass()) {
      return;
    }
    if (this.getInstance()) {
      return;
    }
    (this.node.node as Class).instance = {
      label: this.node.node.label
    };

    this.startEdit();
  }

  hasRecommendations(): boolean {
    return this.isClass() && this.node.recommendations && this.node.recommendations.length > 0;
  }
}
