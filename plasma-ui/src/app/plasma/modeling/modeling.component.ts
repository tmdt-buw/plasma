import {AfterViewChecked, Component, ElementRef, HostListener, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {
  CollisionSchemaDTO,
  CompositeDTO,
  DataModelingControllerService,
  DataSourceDTO,
  DataSourceSchemaDTO, EdgeDTO,
  EntityConceptDTO,
  EntityConceptSuggestionDTO,
  EntityTypeDTO,
  ObjectNodeDTO,
  PrimitiveDTO, PrimitiveEntityTypeEdgeDTO,
  RelationConceptDTO,
  RelationDTO,
  SchemaNodeDTO,
  SetNodeDTO,
  SyntacticOperationDTO
} from '../../../../build/openapi';
import {animate, style, transition, trigger} from '@angular/animations';
import {InformationgraphComponent, SubtreeFilter} from './informationgraph/informationgraph.component';
import {Subject} from 'rxjs';
import {NgbModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {types} from 'typestyle';
import * as Vis from 'vis';
import {debounceTime, distinctUntilChanged} from 'rxjs/operators';
import {DataSourceControllerService} from '../../../../build/openapi/api/dataSourceController.service';
import {AnalysisControllerService} from '../../../../build/openapi/api/analysisController.service';
import {LocalKnowledgeControllerService} from '../../../../build/openapi/api/localKnowledgeController.service';


enum ConceptSelection {
  Entities,
  Relations,
  None
}


@Component({
  templateUrl: './modeling.component.html',
  animations: [
    trigger('sidebarVisible', [
      transition('void => *', [
        style({transform: 'translateX(-100%)'}),
        animate('150ms ease-out', style({transform: 'translateX(0)'}))
      ]),
      transition('* => void', [
        style({transform: 'translateX(0)'}),
        animate('150ms ease-in', style({transform: 'translateX(-100%)'}))
      ])
    ]),
    trigger('sidebarVisibleRight', [
      transition('void => *', [
        style({transform: 'translateX(100%)'}),
        animate('150ms ease-out', style({transform: 'translateX(0)'}))
      ]),
      transition('* => void', [
        style({transform: 'translateX(0)'}),
        animate('150ms ease-in', style({transform: 'translateX(100%)'}))
      ])
    ])
  ]
})

export class ModelingComponent implements OnInit, AfterViewChecked, OnDestroy {


  constructor(
    private dataModelingService: DataModelingControllerService,
    private readonly dataSourceController: DataSourceControllerService,
    private readonly schemaanalysisController: AnalysisControllerService,
    private readonly localKnowledgeGraphController: LocalKnowledgeControllerService,
    private modalService: NgbModal) {


    this.entityConceptFilterDebouncer.pipe(debounceTime(700), distinctUntilChanged())
      .subscribe(async () => {
        await this.filterEntityConcepts();
      });

    this.relationConceptFilterDebouncer.pipe(debounceTime(700), distinctUntilChanged())
      .subscribe(async () => {
        await this.filterRelationConcepts();
      });
  }

  allDataSources: DataSourceDTO[] = [];

  selectedDataSource: DataSourceDTO;
  schemaAnalysisReady: boolean;
  schemaAnalysisExisting: boolean;

  dataSourceSchema: DataSourceSchemaDTO;

  private elem;
  private ontology;


  @ViewChild('informationGraph', {static: false})
  public informationGraph: InformationgraphComponent;

  @ViewChild('informationGraph', {read: ElementRef, static: false})
  public informationGraphRef: ElementRef;

  @ViewChild('syntacticOperationModal', {static: true})
  public syntacticOperationModal: ElementRef;

  @ViewChild('entityConceptAdditionModal', {static: true})
  public entityConceptAdditionModal: ElementRef;

  @ViewChild('relationConceptAdditionModal', {static: true})
  public relationConceptAdditionModal: ElementRef;

  @ViewChild('entityTypeEditModal', {static: true})
  public entityTypeEditModal: ElementRef;

  @ViewChild('exampleValueModal', {static: true})
  public exampleValueModal: ElementRef;

  @ViewChild('container', {static: true})
  public container: ElementRef;

  @ViewChild('contextMenu', {static: false})
  public contextMenu: ElementRef;

  public selectedNode: SchemaNodeDTO | EntityTypeDTO | PrimitiveDTO;
  public selectedEdge: RelationDTO;
  public selectedOperation?: number;
  public selectedConceptTab: ConceptSelection = ConceptSelection.None;
  public lastSelectedConceptTab: ConceptSelection = ConceptSelection.None;
  public operations: SyntacticOperationDTO[];
  public suggestedEntityConcepts: EntityConceptSuggestionDTO[];

  public filteredEntityConcepts: EntityConceptDTO[] = [];
  public filteredRelationConcepts: RelationConceptDTO[] = [];

  public allEntityConcepts: EntityConceptDTO[] = [];
  public allRelationConcepts: RelationConceptDTO[] = [];

  public recentlyUsedEntityConcepts: EntityConceptDTO[] = [];
  public recentlyUsedRelationConcepts: RelationConceptDTO[] = [];
  public entityConceptFilter: string;
  public relationConceptFilter: string;
  public entityConceptFilterDebouncer = new Subject<string>();
  public relationConceptFilterDebouncer = new Subject<string>();

  public newEntityConcept: EntityConceptDTO;
  public newRelationConcept: RelationConceptDTO;
  public selectedRelationConcept: RelationConceptDTO | null;
  public lastSelectedRelationConcept: RelationConceptDTO | null;
  public selectedEntityType: EntityTypeDTO;

  public style = {};
  public selectedSubtreeFilter: SubtreeFilter = SubtreeFilter.Both;
  public selectedTreeFilter = '';
  public showNodeInformation = false;

  public contextMenuStyle: types.CSSProperties | null = null;

  public readonly ConceptSelection = ConceptSelection;
  public readonly SubtreeFilter = SubtreeFilter;

  private syntacticOperationModalNgb: NgbModalRef;
  private entityConceptAdditionModalNgb: NgbModalRef;
  private relationConceptAdditionModalNgb: NgbModalRef;
  private entityTypeEditModalNgb: NgbModalRef;
  private exampleValueModalNgb: NgbModalRef;
  errors: string;
  schema: DataSourceSchemaDTO;

  modelingInitialized = false;
  changeComplete = false;

  private static calculateContainerHeight() {
    const height = window.innerHeight - 280;
    if (height > 200) {
      return height;
    } else {
      return 200;
    }
  }

  public async ngOnInit() {

    this.elem = document.documentElement;

    this.allDataSources = await this.dataSourceController.getAllDataSources().toPromise();

    this.allDataSources.sort((a, b) => {
      return (a.title.toUpperCase() < b.title.toUpperCase()) ? -1 : (a.title.toUpperCase() > b.title.toUpperCase()) ? 1 : 0;

    });

    // tslint:disable-next-line:max-line-length

    this.schema = await this.dataModelingService.getDataSourceSchema(this.selectedDataSource.uuid).toPromise();

    await this.prepareDataSource();

  }

  async prepareDataSource() {

    await this.filterEntityConcepts();
    await this.filterRelationConcepts();

    setTimeout(() => {
      const network = this.informationGraph.network.network;
      this.schema.syntaxModel.nodes.forEach((value) => {
        if (value.x === null && value.y === null) {
          network.storePositions();

          let tempNodes: Vis.Node[] = [];

          if (this.informationGraph.network.nodes instanceof Vis.DataSet) {
            tempNodes = this.informationGraph.network.nodes.get();
          }

          if (this.informationGraph.network.nodes instanceof Array) {
            tempNodes = this.informationGraph.network.nodes;
          }
          tempNodes.forEach((v) => {
            if (v.id.toString() === value.id) {

              value.x = v.x;
              value.y = v.y;

            }
          });
        }

      });
      this.persistSchemaPositions(this.schema);
      this.informationGraph.getNetwork().fit();
      network.stopSimulation();
      network.setOptions({
        layout: {
          hierarchical: false
        }
      });
    }, 1000);
  }

  public ngOnDestroy() {
    if (document.fullscreenElement) {
      document.exitFullscreen();
    }
  }

  public ngAfterViewChecked() {
    this.onResize();
  }

  public async undo() {
    this.schema = await this.dataModelingService.undoDataSourceSchemaModification(this.selectedDataSource.uuid).toPromise();
  }

  public async redo() {
    this.schema = await this.dataModelingService.redoDataSourceSchemaModification(this.selectedDataSource.uuid).toPromise();
  }

  public toggleProperty(name: string) {
    if (this.newRelationConcept.properties.indexOf(name) > -1) {
      this.newRelationConcept.properties.splice(this.newRelationConcept.properties.indexOf(name), 1);
    } else {
      this.newRelationConcept.properties.push(name);
    }
  }

  public showExampleValues() {

    this.exampleValueModalNgb = this.modalService.open(this.exampleValueModal, {size: 'lg'});
    this.exampleValueModalNgb.result.then(async () => {
      return;
    }, () => {
      return;
    });
  }

  public openContextMenu(event: Vis.Properties) {
    const mouseEvent: MouseEvent = event.event as any;
    mouseEvent.preventDefault();
    this.informationGraph.network.disableEditMode();

    let open = false;

    const relationId = this.informationGraph.getNetwork().getEdgeAt(event.pointer.DOM);
    if (relationId) {
      const relation = this.getSemanticModelEdge(relationId);
      if (relation) {
        open = true;
        this.selectedEdge = relation;
        this.selectedNode = null;
      }
    }

    const nodeId = this.informationGraph.getNetwork().getNodeAt(event.pointer.DOM);
    if (nodeId) {
      open = true;
      this.selectedEdge = null;
      if (this.isNodeInSemanticModel(nodeId)) {
        this.selectedNode = this.getSemanticModelNode(nodeId);
        const mappedPrimitive: PrimitiveDTO = this.getMappedPrimitiveNode(this.selectedNode.id);
        this.operations = mappedPrimitive ? mappedPrimitive.operations : [];
        this.suggestedEntityConcepts = mappedPrimitive ? mappedPrimitive.suggestedEntityConcepts : [];
      } else {
        this.selectedNode = this.getSyntaxModelNode(nodeId);
        if ('operations' in this.selectedNode) {
          this.operations = this.selectedNode.operations;
        }
        this.suggestedEntityConcepts =
          ('suggestedEntityConcepts' in this.selectedNode) ? this.selectedNode.suggestedEntityConcepts : [];
        if (this.operations.length === 0) {
          open = false;
        }
      }
    }

    if (open) {

      this.contextMenuStyle = {
        top: event.pointer.DOM.y + 'px',
        left: event.pointer.DOM.x + 'px',
        position: 'absolute',
        visibility: 'visible'
      };

    }
  }

  public updateContextMenuPosition() {
    if (!this.contextMenu) {
      return;
    }

    const bottom = this.contextMenu.nativeElement.scrollHeight + this.contextMenu.nativeElement.offsetTop;
    const height = ModelingComponent.calculateContainerHeight();
    if (bottom > height) {
      this.contextMenuStyle.top = parseInt(String(this.contextMenuStyle.top), 10) - (bottom - height) + 'px';
    }

    const right = this.contextMenu.nativeElement.scrollWidth + this.contextMenu.nativeElement.offsetLeft;
    if (right > window.innerWidth) {
      this.contextMenuStyle.left = parseInt(String(this.contextMenuStyle.left), 10) - (right - window.innerWidth) + 'px';
    }

    if (this.contextMenuStyle.visibility === 'hidden') {
      this.contextMenuStyle.visibility = 'visible';
    }
  }

  @HostListener('window:click')
  @HostListener('window:keydown.esc')
  public closeContextMenu() {
    this.contextMenuStyle = null;
  }

  public editEntityType(entityType: EntityTypeDTO) {
    this.selectedEntityType = entityType;
    this.entityTypeEditModalNgb = this.modalService.open(this.entityTypeEditModal, {size: 'lg'});
    this.entityTypeEditModalNgb.result.then(async () => {
      this.schema = await this.dataModelingService
        .updateEntityType(this.selectedDataSource.uuid, Number(this.selectedEntityType.id), this.selectedEntityType).toPromise();
    }, () => {
      return;
    });
  }

  public onSelectOperation(index: number) {
    this.selectedOperation = index;
    if (!('operations' in this.selectedNode || this.getMappedPrimitiveNode(this.selectedNode.id))) {
      throw new Error('Called operation selection with an unmapped semantic model node');
    }

    const syntaxNode: SchemaNodeDTO | PrimitiveDTO | EntityTypeDTO =
      this.getMappedPrimitiveNode(this.selectedNode.id) ? this.getMappedPrimitiveNode(this.selectedNode.id) : this.selectedNode;

    this.syntacticOperationModalNgb = this.modalService.open(this.syntacticOperationModal, {size: 'lg'});
    this.syntacticOperationModalNgb.result.then(async () => {
      await this.performSyntacticOperation((syntaxNode as PrimitiveDTO).operations[this.selectedOperation]);
      this.selectedOperation = undefined;
    }, () => {
      this.selectedOperation = undefined;
    });
  }

  public async performSyntacticOperation(operation: SyntacticOperationDTO) {
    this.syntacticOperationModalNgb.close();
    this.schema = await this.dataModelingService.modifySchema(this.selectedDataSource.uuid, operation).toPromise();
  }

  public onSelectEntitiesTab(): void {
    this.selectedConceptTab = ConceptSelection.Entities;
    this.informationGraph.network.disableEditMode();
    this.lastSelectedConceptTab = ConceptSelection.Entities;
  }

  public onSelectRelationsTab(): void {
    this.selectedConceptTab = ConceptSelection.Relations;
    this.lastSelectedConceptTab = ConceptSelection.Relations;
  }

  public addRelationConcept(concept: RelationConceptDTO | EntityConceptDTO) {
    this.selectedRelationConcept = concept as RelationConceptDTO;
    this.lastSelectedRelationConcept = concept as RelationConceptDTO;
    this.informationGraph.network.addEdgeMode();
  }

  public async edgeAdded(edge: Vis.Edge) {
    this.schema = await this.dataModelingService.addRelation(this.selectedDataSource.uuid, Object.assign({
      id: null,
      description: this.selectedRelationConcept.description,
      concept: this.selectedRelationConcept
    }, edge as { from: string, to: string })).toPromise();
    if (this.recentlyUsedRelationConcepts.some((concept) => concept.id === this.selectedRelationConcept.id)) {
      this.recentlyUsedRelationConcepts.splice(
        this.recentlyUsedRelationConcepts.findIndex((concept) => concept.id === this.selectedRelationConcept.id), 1);
    }
    this.recentlyUsedRelationConcepts.unshift(this.selectedRelationConcept);
    this.selectedRelationConcept = null;
  }

  public async createAndAssignConcept(node: PrimitiveDTO): Promise<void> {
    // check if label already exists as a Concept
    let newEntityConcept: EntityConceptDTO | undefined =
      (await this.dataModelingService.getEntityConcepts(this.selectedDataSource.uuid, node.label).toPromise())
        .find((ec) => ec.name === node.label);

    if (!newEntityConcept) {
      // try to create new Entity Concept with the label via modal
      newEntityConcept = await this.onAddEntityConcept(node.label);
    }
    // only if user created EC (user can cancel modal)
    if (newEntityConcept) {
      // set it on this node
      await this.setEntityConceptOfPrimitive(node, newEntityConcept);
    }
  }

  public async onAddEntityConcept(name?: string): Promise<EntityConceptDTO> {
    this.newEntityConcept = {
      id: null,
      uuid: null,
      name: name || '',
      description: ''
    };
    this.entityConceptAdditionModalNgb = this.modalService.open(this.entityConceptAdditionModal, {size: 'lg'});
    try {
      // wait for user input
      await this.entityConceptAdditionModalNgb.result;
      // create the temp EC
      const newEntityConceptDTO: EntityConceptDTO =
        await this.dataModelingService.cacheEntityConcept(this.selectedDataSource.uuid, this.newEntityConcept).toPromise();
      // update UI
      this.recentlyUsedEntityConcepts.unshift(this.transformEntityConceptToUI(newEntityConceptDTO));
      this.filteredEntityConcepts.unshift(this.transformEntityConceptToUI(newEntityConceptDTO));
      return newEntityConceptDTO;
    } catch (error) {
      // check for user dismiss
      if (error !== 'Cross click') {
        throw error;
      }
    }
  }

  public doubleclick(event: Vis.Properties) {
    // no multi click support, just take first!
    const nodeId = event.nodes[0];
    if (this.isNodeInSemanticModel(nodeId)) {
      this.editEntityType(this.getSemanticModelNode(nodeId));
    }
  }

  public selectNode(event: Vis.Properties) {
    // no multi click support, just take first!
    const nodeId = event.nodes[0];
    if (this.isNodeInSemanticModel(nodeId)) {
      this.selectedNode = this.getSemanticModelNode(nodeId);
    } else if (this.isNodeInSyntaxModel(nodeId)) {
      this.selectedNode = this.getSyntaxModelNode(nodeId);
    }
    this.showNodeInformation = true;
  }

  public deselectNode(event: any) {
    this.showNodeInformation = false;
  }

  public onAddRelationConcept(name?: string): void {
    this.newRelationConcept = {
      id: null,
      uuid: null,
      name: name || '',
      description: '',
      properties: []
    };
    this.relationConceptAdditionModalNgb = this.modalService.open(this.relationConceptAdditionModal, {size: 'lg'});
    this.relationConceptAdditionModalNgb.result.then(async () => {

      const newRelationConcept = await this.dataModelingService
        .cacheRelationConcept(this.selectedDataSource.uuid, this.newRelationConcept).toPromise();
      this.recentlyUsedRelationConcepts.unshift(newRelationConcept);
      this.filteredRelationConcepts.unshift(newRelationConcept);
    }, () => {
      return;
    });
  }

  public async filterEntityConcepts(): Promise<void> {
    if (this.entityConceptFilter && this.entityConceptFilter.length > 0) {
      this.filteredEntityConcepts = this.allEntityConcepts
        .filter((ec: EntityConceptDTO) => ec.name.toLowerCase().includes(this.entityConceptFilter.toLowerCase())
          || ec.description.toLowerCase().includes(this.entityConceptFilter.toLowerCase()));
    } else {
      this.filteredEntityConcepts =
        (await this.dataModelingService
          .getEntityConcepts(this.selectedDataSource.uuid, this.entityConceptFilter ? this.entityConceptFilter : '').toPromise())
          .map(this.transformEntityConceptToUI.bind(this));
      this.allEntityConcepts = this.filteredEntityConcepts;
    }
  }

  public async filterRelationConcepts(): Promise<void> {
    if (this.relationConceptFilter && this.relationConceptFilter.length > 0) {
      this.filteredRelationConcepts = this.allRelationConcepts
        .filter((rc: RelationConceptDTO) => rc.name.toLowerCase().includes(this.relationConceptFilter.toLowerCase())
          || rc.description.toLowerCase().includes(this.relationConceptFilter.toLowerCase()));
    } else {
      this.filteredRelationConcepts = await this.dataModelingService
        .getRelationConcepts(this.selectedDataSource.uuid, this.relationConceptFilter ? this.relationConceptFilter : '').toPromise();
      this.allRelationConcepts = this.filteredRelationConcepts;
    }
  }

  public async persistSchemaPositions(schema: DataSourceSchemaDTO) {

    await this.dataModelingService.updatePositions(this.selectedDataSource.uuid, schema).toPromise();
  }

  public startEntityConceptDrag(event: DragEvent, concept: EntityConceptDTO & {dragImage: any}) {

    event.dataTransfer.setData('concept', JSON.stringify(concept));
    event.dataTransfer.setData('conceptType', JSON.stringify(this.selectedConceptTab));
    event.dataTransfer.setDragImage(concept.dragImage, 0, 0);
  }

  public async drop(event: DragEvent) {
    event.preventDefault();
    const concept: EntityConceptDTO = JSON.parse(event.dataTransfer.getData('concept'));
    if (this.recentlyUsedEntityConcepts.some((currentconcept) => currentconcept.id === concept.id)) {
      this.recentlyUsedEntityConcepts.splice(
        this.recentlyUsedEntityConcepts.findIndex((currentconcept) => currentconcept.id === concept.id), 1);
    }
    this.recentlyUsedEntityConcepts.unshift(this.transformEntityConceptToUI(concept));
    const xDom = event.clientX - (this.informationGraphRef.nativeElement.getBoundingClientRect().left + window.scrollX);
    const yDom = event.clientY - (this.informationGraphRef.nativeElement.getBoundingClientRect().top + window.scrollX);
    const droppedOnNodeId = this.informationGraph.getNetwork().getNodeAt({x: xDom, y: yDom});
    let droppedOnNode: SchemaNodeDTO;
    if (droppedOnNodeId !== undefined) {
      droppedOnNode = this.schema.syntaxModel.nodes.find((node) => node.id === droppedOnNodeId);
      if (droppedOnNode['@class'] !== 'PrimitiveDTO') {
        droppedOnNode = undefined;
      }
    }
    const pos = this.informationGraph.getNetwork().DOMToCanvas({x: xDom, y: yDom});
    const entityType = {
      id: null,
      uuid: null,
      label: droppedOnNode ? droppedOnNode.label : concept.name,
      originalLabel: droppedOnNode ? droppedOnNode.label : 'NOT_PART_OF_THE_SYNTAX_MODEL',
      description: concept.description,
      concept,
      x: pos.x,
      y: pos.y,
      mappedToData: null
    };

    if (droppedOnNode) {
      this.schema = await this.dataModelingService.addEntityType(this.selectedDataSource.uuid, entityType, droppedOnNode.id).toPromise();
    } else {
      this.schema = await this.dataModelingService.addEntityType(this.selectedDataSource.uuid, entityType).toPromise();
    }
  }

  public async setEntityConceptOfPrimitive(node: PrimitiveDTO, concept: EntityConceptDTO) {
    const entityType = {
      id: null,
      uuid: null,
      label: concept.name,
      originalLabel: node.label,
      description: concept.description,
      concept,
      x: node.x,
      y: node.y,
      mappedToData: null
    };
    this.schema = await this.dataModelingService.addEntityType(this.selectedDataSource.uuid, entityType, node.id).toPromise();
  }


  public async dragEnd(event: Vis.Properties) {
    const dropOn = this.informationGraph.getNetwork().getNodeAt(event.pointer.DOM);

    for (const nodeId of event.nodes) {
      if (this.isNodeInSemanticModel(nodeId)) {
        if (dropOn
          && this.isNodeInSyntaxModel(dropOn)
          && this.getSyntaxModelNode(dropOn)['@class']
          === 'PrimitiveDTO') {
          await this.connectEntityTypeToSyntaxModel(dropOn, nodeId);
        } else {


          const mUuid: string = this.selectedDataSource.uuid;
          const mEntityTypeId: number = Number(this.getSemanticModelNode(nodeId).id);
          const mEntityTypeDto: EntityTypeDTO = this.getSemanticModelNode(nodeId);
          mEntityTypeDto.x = event.pointer.canvas.x;
          mEntityTypeDto.y = event.pointer.canvas.y;

          await this.dataModelingService.positionEntityType(mUuid, mEntityTypeId, mEntityTypeDto).toPromise();


        }
      } else if (this.isNodeInSyntaxModel(nodeId)) {
        // tslint:disable-next-line:max-line-length
        await this.dataModelingService.positionSchemaNode(this.selectedDataSource.uuid, nodeId, (Object.assign(this.getSyntaxModelNode(nodeId), {
          x: event.pointer.canvas.x,
          y: event.pointer.canvas.y
        })) as SchemaNodeDTO | CollisionSchemaDTO | CompositeDTO | ObjectNodeDTO | PrimitiveDTO | SetNodeDTO).toPromise();
      }
    }
  }



  public dragover(event: DragEvent) {
    event.preventDefault();
  }

  public async finishModeling() {
      this.schema = await this.dataModelingService.finishModeling(this.selectedDataSource.uuid).toPromise();
  }

  public async deleteRelation(relation: RelationDTO) {
    this.schema = await this.dataModelingService.removeRelation(this.selectedDataSource.uuid, Number(relation.id)).toPromise();
  }

  public async deleteEntityType(entityType: EntityTypeDTO) {
    if (this.entityTypeEditModalNgb) {
      this.entityTypeEditModalNgb.dismiss();
    }
    this.schema = await this.dataModelingService.removeEntityType(this.selectedDataSource.uuid, Number(entityType.id)).toPromise();
  }

  @HostListener('window:resize')
  public onResize() {
    const height = ModelingComponent.calculateContainerHeight();
    this.style = {height: `${height}px`};
    this.updateContextMenuPosition();
  }

  private isNodeInSyntaxModel(nodeId: Vis.IdType) {
    for (const node of this.schema.syntaxModel.nodes) {
      if (node.id === nodeId) {
        return true;
      }
    }
  }

  private isNodeInSemanticModel(nodeId: Vis.IdType) {
    for (const node of this.schema.semanticModel.nodes) {
      if (node.id === nodeId) {
        return true;
      }
    }
  }

  private getSemanticModelNode(nodeId: Vis.IdType): EntityTypeDTO {
    for (const node of this.schema.semanticModel.nodes) {
      if (node.id === nodeId) {
        return node;
      }
    }
  }

  private getSyntaxModelNode(nodeId: Vis.IdType): SchemaNodeDTO | PrimitiveDTO {
    for (const node of this.schema.syntaxModel.nodes) {
      if (node.id === nodeId) {
        return node;
      }
    }
  }

  private getSemanticModelEdge(edgeId: Vis.IdType) {
    for (const edge of this.schema.semanticModel.edges) {
      if (edge.id === edgeId) {
        return edge;
      }
    }
  }

  private async connectEntityTypeToSyntaxModel(syntaxModelNodeId: Vis.IdType, entityTypeId: Vis.IdType) {
    for (const node of this.schema.syntaxModel.nodes) {
      if (node.id === syntaxModelNodeId) {
        for (const operation of node.operations) {
          if (operation.name === 'ConnectEntityType') {
            for (const parameter of operation.parameter.value) {
              // @ts-ignore
              if (parameter.type === 'EntityTypeId') {
                // @ts-ignore
                parameter.value = [entityTypeId];
              }
            }
            this.schema = await this.dataModelingService.modifySchema(this.selectedDataSource.uuid, operation).toPromise();
            return;
          }
        }
      }
    }
    throw Error('Could not connect entity type and syntax node because the id of the syntax node could not be found.');
  }

  private transformEntityConceptToUI(concept: EntityConceptDTO) {
    const dragImage = document.createElement('img');
    const node = {
      id: null, uuid: null, label: concept.name, description: null,
      concept, x: null, y: null, mappedToData: null, originalLabel: null
    };
    dragImage.src = this.informationGraph.getEntityNodeSVG(node);
    return Object.assign(concept, {dragImage});
  }

  public async deleteTemporaryEntityConcept(entityConcept: EntityConceptDTO): Promise<void> {

    const currentConceptMappings = this.schema.semanticModel.nodes.map((node) => node.concept.id);
    if (currentConceptMappings.indexOf(entityConcept.id) !== -1) {
      // tslint:disable-next-line:max-line-length
      this.errors = 'Please delete all occurrences of the entity concept "' + entityConcept.name + '" from the graph before deleting it, using the mouse right click!';
    } else {

      this.filteredEntityConcepts =
        (await (await this.dataModelingService.uncacheEntityConcept(this.selectedDataSource.uuid, Number(entityConcept.id)).toPromise())
          .map(this.transformEntityConceptToUI.bind(this)));

      const i = this.recentlyUsedEntityConcepts.indexOf(entityConcept);
      if (i > -1) {
        this.recentlyUsedEntityConcepts.splice(i, 1);
      }
    }
  }

  public async deleteTemporaryRelationConcept(relationConcept: RelationConceptDTO) {

    const currentRelationMappings = this.schema.semanticModel.edges.map((edge) => edge.concept.id);

    if (currentRelationMappings.indexOf(relationConcept.id) !== -1) {
      // tslint:disable-next-line:max-line-length
      this.errors = 'Please delete all occurrences of the relation concept "' + relationConcept.name + '" from the graph before deleting it, using the mouse right click!';
    } else {

      this.filteredRelationConcepts =
        (await this.dataModelingService.uncacheRelationConcept(this.selectedDataSource.uuid, Number(relationConcept.id)).toPromise());

      const i = this.recentlyUsedRelationConcepts.indexOf(relationConcept);
      if (i > -1) {
        this.recentlyUsedRelationConcepts.splice(i, 1);
      }
    }
  }

  public async changeDatasource() {
    this.changeComplete = false;
    await this.isReady();


    if (this.schemaAnalysisReady) {
      this.dataSourceSchema = null;
      // tslint:disable-next-line:max-line-length

      let flagged = false;

      await this.dataModelingService
        .getDataSourceSchema(this.selectedDataSource.uuid).toPromise()
        .then(res => {this.modelingInitialized = true; this.schema = res; console.log(this.modelingInitialized); flagged = true; })
        .catch(() => {this.modelingInitialized = false; console.log(this.modelingInitialized); });

      if (!flagged) {
        this.modelingInitialized = false;
        console.log('modeling not initialized');
      }


      this.entityConceptFilterDebouncer.pipe(debounceTime(700), distinctUntilChanged())
        .subscribe(async () => {
          await this.filterEntityConcepts();
        });

      this.relationConceptFilterDebouncer.pipe(debounceTime(700), distinctUntilChanged())
        .subscribe(async () => {
          await this.filterRelationConcepts();
        });

      this.prepareDataSource();

    } else {
      this.modelingInitialized = false;
    }
    this.changeComplete = true;

  }

  public init() {
    this.dataModelingService.initDataSourceSchema(this.selectedDataSource.uuid).subscribe(() => this.changeDatasource());
  }

  public async isReady() {
    this.schemaAnalysisExisting = await this.schemaanalysisController.existing(this.selectedDataSource.uuid).toPromise();
    if (this.schemaAnalysisExisting) {
      this.schemaAnalysisReady = await this.schemaanalysisController.isReady(this.selectedDataSource.uuid).toPromise();
    } else {
      this.schemaAnalysisReady = false;
    }
  }

  public onSelectConceptsTab(): void {
    if (this.lastSelectedConceptTab === ConceptSelection.None && this.selectedConceptTab === ConceptSelection.None) {
      this.onSelectEntitiesTab();
    } else if (this.lastSelectedConceptTab !== ConceptSelection.None && this.selectedConceptTab === this.lastSelectedConceptTab) {
      this.deactivateRelationConcept();
      this.selectedConceptTab = ConceptSelection.None;
    } else if (this.lastSelectedConceptTab === ConceptSelection.Entities) {
      this.onSelectEntitiesTab();
    } else {
      this.onSelectRelationsTab();
    }
  }

  public clickRelationConcept(concept: RelationConceptDTO | EntityConceptDTO) {
    if (concept as RelationConceptDTO === this.lastSelectedRelationConcept) {
      this.deactivateRelationConcept();
    } else {
      this.addRelationConcept(concept);
    }
  }

  public deactivateRelationConcept() {
    this.selectedRelationConcept = null;
    this.lastSelectedRelationConcept = null;
    this.informationGraph.network.disableEditMode();
  }

  public switchFullScreen() {
    if (document) {
      if (document.fullscreenElement) {
        document.exitFullscreen();
      } else {
        document.documentElement.requestFullscreen();
      }
    }
  }

  getMappedPrimitiveNode(entityNodeId: string): PrimitiveDTO {
    const mapping = this.schema.primitiveToEntityTypeList.find(p => p.to === entityNodeId);
    if (mapping) {
      const primitive = this.schema.syntaxModel.nodes.find(p => p.id === mapping.mappedPrimitive);
      return primitive as PrimitiveDTO;
    }
    return null;
  }

  /*
  This method is used to extract all relevant data after modeling is finished
  This includes
  - a flattened rdf file of the model
  - a not flattened rdf file of the model
  - a json file containing all mappings
  - a json file contain not mapped attributes
   */
  public async extractSemanticModel() {

    const fileName = this.selectedDataSource.title.split(' ')[0] + '.rdf';
    const fileNameFlat = this.selectedDataSource.title.split(' ')[0] + '_flat.rdf';
    const fileNameMappedPrimitives = this.selectedDataSource.title.split(' ')[0] + '_mapped_attributes.json';
    const fileNameUnmappedPrimitives = this.selectedDataSource.title.split(' ')[0] + '_unmapped_attributes.json';

    // Export unflattened model
    try {
      this.ontology = await this.localKnowledgeGraphController
        .convertSemanticModel(this.schema.semanticModel.uuid, 'xml', false).toPromise();
      this.download(fileName, this.ontology);
    } catch (e) {console.log('could not download unflattened file'); }

    // Export flattened model
    try {
      this.ontology = await this.localKnowledgeGraphController
        .convertSemanticModel(this.schema.semanticModel.uuid, 'xml', true).toPromise();
      this.download(fileNameFlat, this.ontology);
    } catch (e) {console.log('could not download flattened file'); }

    // Export Mappings
    const primitiveMappings = [];

    this.schema.primitiveToEntityTypeList.forEach((x: PrimitiveEntityTypeEdgeDTO) => {
      this.schema.semanticModel.nodes.forEach((y: EntityTypeDTO) => {
        if (y.id === x.to) {
          let foundRoot = false;
          let foundPath = [];
          let currentElement = this.getMappedPrimitiveNode(x.to).id;
          while (foundRoot === false) {
            const newElementId = this.schema.syntaxModel.edges.find((edge: EdgeDTO) => edge.to === currentElement).from;
            if (newElementId === this.schema.syntaxModel.root) {
              foundRoot = true;
            } else {
              const newName = this.schema.syntaxModel.nodes.find((node: SchemaNodeDTO) => node.id === newElementId).label;
              foundPath = [newName].concat(foundPath);
              currentElement = newElementId;
            }
          }
          let originalLabel = this.getMappedPrimitiveNode(x.to).label;
          if (originalLabel === '*') {
            originalLabel = foundPath.pop();
          }
          primitiveMappings.push({concept: y.concept.sourceURI,
            conceptId: y.concept.uuid,
            originalLabel,
            path: foundPath});
        }
      });
    });
    this.downloadJson(fileNameMappedPrimitives, JSON.stringify(primitiveMappings));

    // unmapped primitives
    const unmappedPrimitives = [];
    this.schema.syntaxModel.nodes.forEach((x: SchemaNodeDTO) => {

      let found = false;
      if (x['@class'] === 'PrimitiveDTO') {

        primitiveMappings.forEach((y) => {
          if (x.label === y.originalLabel) {
            found = true;
          }
        });
        if (!found) {
          if (x.label !== '*') {

            unmappedPrimitives.push(x.label);
          }
        }
      }
    });
    this.downloadJson(fileNameUnmappedPrimitives, JSON.stringify(unmappedPrimitives));
  }

  private download(filename: string, content: string) {
    const blob = new Blob([content], { type: 'application/xml' });
    const link = document.createElement('a');
    link.setAttribute('type', 'hidden');
    link.href = window.URL.createObjectURL(blob);
    link.download = filename;
    link.target = '_blank';
    document.body.appendChild(link);
    link.click();
    link.remove();
  }

  private downloadJson(filename: string, content: string) {
    const blob = new Blob([content], { type: 'application/json' });
    const link = document.createElement('a');
    link.setAttribute('type', 'hidden');
    link.href = window.URL.createObjectURL(blob);
    link.download = filename;
    link.target = '_blank';
    document.body.appendChild(link);
    link.click();
    link.remove();
  }

}

