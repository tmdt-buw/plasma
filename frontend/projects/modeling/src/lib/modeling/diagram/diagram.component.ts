import { Overlay } from '@angular/cdk/overlay';
import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  OnInit,
  Output,
  ViewChild,
  ViewContainerRef,
  ViewEncapsulation
} from '@angular/core';
import * as dagre from 'dagre';
import * as graphlib from 'graphlib';
import { dia, g, highlighters, layout } from 'jointjs';
import { ModalOptions, NzModalRef, NzModalService } from 'ng-zorro-antd/modal';
import { BehaviorSubject, Subject } from 'rxjs';
import { auditTime, distinctUntilChanged, pairwise, skip } from 'rxjs/operators';
import {
  Class,
  CombinedModel,
  DataProperty,
  DeltaModification,
  Instance,
  Literal,
  NamedEntity,
  ObjectProperty,
  PrimitiveNode,
  Relation,
  SchemaNode,
  SemanticModel,
  SemanticModelNode,
  SyntaxModel
} from '../../api/generated/dms';
import { ContextMenuService } from '../context-menu/context-menu.service';
import { PlsSemanticClassMenuComponent } from '../context-menu/semantic-class-menu/semantic-class-menu.component';
import { PlsRelationConceptMenuComponent } from '../context-menu/relation-menu/relation-menu.component';
import { ModalMouseEnabledConfig } from '../dialogs/common/modal.base-config';
import {
  PlsSemanticNodeDetailsComponent,
  PlsSemanticNodeDetailsData
} from '../dialogs/semantic-node-dialog/semantic-node.component';
import { PlsSyntaxNodeDetailsComponent } from '../dialogs/syntax-node-dialog/syntax-node.component';
import { Filter } from '../model/common/filter';
import { Graphs } from '../model/configuration/graph';
import { Icons } from '../model/configuration/icons';
import { Links } from '../model/configuration/link';
import { Nodes, NodeType, SemanticNode, SyntaxNode } from '../model/configuration/node';
import { Papers } from '../model/configuration/paper';
import { NodeChangedEvent } from '../model/events/node-changed-event';
import { ModelingService } from '../modeling.service';
import { v4 as uuidv4 } from 'uuid';
import svgPanZoom from '@dash14/svg-pan-zoom';
import { PlsNamedEntityMenuComponent } from '../context-menu/named-entity-menu/named-entity-menu.component';
import { PlsLiteralMenuComponent } from '../context-menu/literal-menu/literal-menu.component';
import Point = g.Point;


@Component({
  selector: 'pls-modeling-diagram',
  templateUrl: './diagram.component.html',
  styleUrls: ['./diagram.component.less'],
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PlsDiagramComponent implements OnInit, AfterViewInit {

  @Input() viewerMode: boolean = false; // if set to true hides all input and interaction options that modify the model

  get combinedModel(): CombinedModel {
    return this._combinedModel;
  }

  @Input() modelId: string;
  // external controls
  @Input() $center: Subject<any>;
  @Input() $layout: Subject<any>;
  @Input() $filter: BehaviorSubject<Filter>;
  @Input() $URIMode: BehaviorSubject<boolean>;
  private literalValueModalRef: NzModalRef<any>;
  // config
  @Input() primaryColor;
  @Input() primaryColorContrast;
  @Input() accentColor;
  @Input() accentColorContrast;
  // data
  initializing = true;
  _combinedModel: CombinedModel;

  // graph elements
  @ViewChild('graph') canvas: ElementRef;
  @ViewChild('literalValueModalTpl') literalValueModalTpl;
  private graph: dia.Graph;
  private paper: dia.Paper;
  private panAndZoom: any;
  private $resize: BehaviorSubject<{ width: number, height: number }>;

  // dialog
  private nodeDialogRef: NzModalRef<any>;

  // create graph if input schema changes and graph has already been initialized
  @Input() set combinedModel(combinedModel: CombinedModel) {
    this._combinedModel = combinedModel;
    if (!this.initializing) {
      this.createGraph(this.$URIMode?.getValue());
    }
  }

  // modeling
  modelNode: SemanticModelNode; // currently selected node template
  relation: Relation; // currently selected relation template
  relationTarget = false;
  hoveredElement;
  mousePosition;
  recommendations = [];
  ghostLiteral: dia.Element;
  tmpLiteralValue: string;
  literalDefinitionError: string;

  // events
  @Output() graphLaidOut = new EventEmitter<CombinedModel>();
  @Output() semanticModelElementsAdded = new EventEmitter<{ entities?: Array<SemanticModelNode>, relations?: Array<Relation> }>();
  @Output() semanticModelElementsRemoved = new EventEmitter<{ entities?: Array<SemanticModelNode>, relations?: Array<Relation> }>();
  @Output() nodeChanged = new EventEmitter<{ entities?: Array<SemanticModelNode>, nodes?: Array<SchemaNode> }>();
  @Output() nodePositionChanged = new EventEmitter<NodeChangedEvent>();
  @Output() nodeClicked = new EventEmitter<string>();
  @Output() recommendationAccepted = new EventEmitter<DeltaModification>();
  @Output() recomendationRejected = new EventEmitter<DeltaModification>();

  // hidden canvas to calculate label widths
  private _canvas = document.createElement('canvas');

  CustomHighlighters = {
    CONNECTION: 'connection-highlighter'
  };

  constructor(private overlay: Overlay, private modelingService: ModelingService, private contextMenu: ContextMenuService,
              private viewContainerRef: ViewContainerRef, private modal: NzModalService) {
  }

  /**
   * Init everything
   */
  ngOnInit(): void {
    // init joint.js graph
    this.graph = Graphs.createGraph();
    // init joint.js paper
    this.paper = Papers.createPaper(this.graph, () => this.relation, this.viewerMode);
    // create nodes and links
    this.createGraph();
    // subscribe to relation tool
    this.modelingService.relation.subscribe((relationSelection) => {
      this.relation = relationSelection;
    });
    // show / hide recommendations
    this.modelingService.recommendation.subscribe((recommendation) => {
      if (recommendation) {
        this.showRecommendation(recommendation);
      } else {
        this.hideRecommendations();
      }
    });
    // subscribe to external events
    this.$center?.subscribe(() => this.center());
    this.$layout?.subscribe(() => this.layout());
    this.$filter.pipe(
      skip(1),
      distinctUntilChanged()
    ).subscribe(() => {
      this.createGraph();
    });
    this.$URIMode?.pipe(
      distinctUntilChanged()
    ).subscribe((showURI) => {
      this.createGraph(showURI);
    });
  }

  /**
   * Append graph to canvas and register events
   */
  ngAfterViewInit(): void {
    this.canvas.nativeElement.append(this.paper.el);
    this.paper.unfreeze();

    // init pan and zoom
    this.panAndZoom = svgPanZoom(this.canvas.nativeElement.firstChild.childNodes[2],
      {
        viewportSelector: this.canvas.nativeElement.firstChild.childNodes[2].childNodes[1],
        fit: false,
        zoomScaleSensitivity: 0.2,
        panEnabled: false,
        maxZoom: 5,
        minZoom: 0.0001,
        dblClickZoomEnabled: false
      });

    this.panAndZoom.setOnPan(() => {
      this.contextMenu.close(null);
      this.unhighlightAll();
    });
    this.panAndZoom.setOnZoom(() => {
      this.contextMenu.close(null);
      this.unhighlightAll();
    });

    this.paper.on({
      // add selection for nodes
      'element:pointerclick': (elementView: dia.ElementView) => {
        this.unhighlightAll();
        elementView.highlight();
        const model = elementView.model.attributes;
        if (!this.viewerMode) {
          this.openDetails(model.id, model.type);
        }
        if (Nodes.isSemanticNode(elementView)) {
          elementView.model.toFront();
        }
      },
      // emit node id
      'element:pointerdown': (elementView: dia.ElementView) => {
        const model = elementView.model.attributes;
        this.nodeClicked.emit(model.id);
      },
      // adjust edges and persist element position on pointer up
      'element:pointerup': (elementView: dia.ElementView, evt, x, y) => {
        const coordinates = new Point(x, y);
        const clazzId = elementView.model.id;
        const elementBelow = this.graph.findModelsFromPoint(coordinates).find((el) => {
          return (el.id !== clazzId);
        });
        if (Nodes.isClass(elementView) && elementBelow) {
          const syntaxNode = this.findSyntaxNodeById(elementBelow.id as string);
          const classNode = this.findSemanticNodeById(clazzId as string) as Class;
          if (syntaxNode._class === 'PrimitiveNode' && !classNode.syntaxNodeUuid) {
            // TODO add a confirm here?
            if (!classNode.instance) {
              classNode.instance = {
                label: syntaxNode.label,
                uuid: uuidv4(),
              };
            }
            classNode.syntaxNodeUuid = syntaxNode.uuid;
            classNode.syntaxLabel = syntaxNode.label;
            classNode.syntaxPath = syntaxNode.path.join('->');
            classNode.instance.label = syntaxNode.label;
            classNode.x = syntaxNode.x;
            classNode.y = syntaxNode.y;
            const nodeChanges = {entities: [classNode]};
            this.nodeChanged.emit(nodeChanges);
            return;
          }
        }
        // console.log('elementView', elementView);
        Links.adjustVertices(this.graph, elementView);
        const element = elementView.model;
        this.updatePosition(element, true);
      },
      'cell:mouseover': () => {
      },
      // Highlights for modeling
      'element:mouseover': (elementView: dia.ElementView) => {
        // console.log('element:mouseover');
        if (this.modelNode?._class === 'Class' && this.getIfLeafNode(elementView)) {
          elementView.highlight();
          this.hoveredElement = elementView;
        }

        /*
        // TODO only show recommendations if hovered for 0,5s
        const nodeRecommendations = this.filterRecommendationsForAnchor(elementView.model.id as string);
        console.log('mouseover', elementView.model.id, this.schema.recommendations, nodeRecommendations);
        this.showRecommendations(nodeRecommendations);
        console.log('layout elements', this.recommendations.concat(elementView).filter(cell => cell.attributes?.type === Nodes.semanticNodeName));
        const bbox = elementView.getBBox().clone();
        console.log('Root element ' + bbox.x + '/' + bbox.y);
        */

      },
      'cell:mouseleave': () => {
        // console.log('cell:mouseleave');
      },
      'element:mouseleave': () => {
        // console.log('element:mouseleave');
        if (this.modelNode) {
          this.unhighlightAll();
        }
        this.hideRecommendations();
        this.hoveredElement = null;
      },
      // handle linking of nodes
      'link:connect': (linkView: dia.LinkView, evt) => {
        const model = linkView.model.attributes;
        const from = model.source.id;
        const to = model.target.id;
        if (this.relation?._class === 'ObjectProperty') {
          const relation: ObjectProperty = {
            _class: 'ObjectProperty',
            from,
            to,
            label: this.relation.label,
            uri: this.relation.uri
          };
          this.semanticModelElementsAdded.emit({relations: [relation]});
        }
      },
      // remove highlight and close dialog if clicked in blank zone
      'blank:pointerclick': () => {
        this.unhighlightAll();
        this.modelingService.relation.next(null);
      },
      // Enable pan when mouse button down in blank zone
      'blank:pointerdown': () => {
        this.panAndZoom.enablePan();
      },
      'blank:pointerup': () => {
        this.panAndZoom.disablePan();
      },
      'blank:mouseover': (evt) => {
        /* EXPERIMENTAL FEATURE
        console.log('blank:mouseover', evt);
        if (this.relation?._class === 'DataProperty') {
          if (!this.ghostLiteral) {
            this.ghostLiteral = this.addLiteralNode(uuidv4(), 'add literal', 'text_fields', evt.x, evt.y);
          }
          this.ghostLiteral.position(evt.x, evt.y);
        }
        */
      },
      // Disable pan when the mouse button is released
      'cell:pointerup': () => {
        this.panAndZoom.disablePan();
      },
      'link:pointerup': (link, evt, x, y) => {
        const coordinates = new Point(x, y);
        const source = link.sourceView?.model?.id;
        const elementBelow = this.graph.findModelsFromPoint(coordinates).find((el) => {
          return (el.id !== source);
        });
        if (this.relation?._class === 'DataProperty' && !elementBelow) {
          const literal: Literal = {
            _class: 'Literal',
            x,
            y,
            value: '',
            uuid: uuidv4()
          };
          const from = source;
          const to = literal.uuid;
          const relation: DataProperty = {
            _class: 'DataProperty',
            from,
            to,
            label: this.relation.label,
            uri: this.relation.uri
          };
          const tmpNode: dia.Element = this.addLiteralNode(literal.uuid, 'Set Value', 'text_fields', literal.x, literal.y);
          const tmpRel = this.addDataPropertyLink(relation.uuid, relation.from, relation.to, relation.label);

          this.literalValueModalRef = this.modal.create({
            nzStyle: {
              position: 'absolute',
              top: (this.mousePosition.y - 50) + 'px',
              left: (this.mousePosition.x - 150) + 'px',
              width: '300px',
              height: '200px'
            },
            nzBodyStyle: {
              padding: '3px'
            },
            // nzTitle: 'Enter literal value',
            nzContent: this.literalValueModalTpl,
            nzClosable: false,
            nzOnOk: () => {
              if (!this.tmpLiteralValue) {
                this.literalDefinitionError = 'Empty string is not allowed.';
                return false;
              }
              literal.label = this.tmpLiteralValue;
              this.literalDefinitionError = undefined;
              this.tmpLiteralValue = undefined;
              this.semanticModelElementsAdded.emit({entities: [literal], relations: [relation]});
            },
            nzOnCancel: () => {
              this.graph.removeCells([tmpNode, tmpRel]);
              this.literalDefinitionError = undefined;
              this.tmpLiteralValue = undefined;
            }
          });
        } else if (this.relation?._class === 'DataProperty' && elementBelow) {
          if (Nodes.isPrimitiveNode(elementBelow)) {
            const syntaxNode: PrimitiveNode = this.findSyntaxNodeById(elementBelow.id as string);
            const literal: Literal = {
              _class: 'Literal',
              x,
              y,
              value: syntaxNode.label,
              uuid: uuidv4(),
              syntaxNodeUuid: syntaxNode.uuid,
              syntaxLabel: syntaxNode.label,
              syntaxPath: syntaxNode.path.join('->'),
            };
            const from = source;
            const to = literal.uuid;
            const relation: DataProperty = {
              _class: 'DataProperty',
              from,
              to,
              label: this.relation.label,
              uri: this.relation.uri
            };
            const changes = {entities: [literal], relations: [relation]};
            this.semanticModelElementsAdded.emit(changes);
            return;
          }

        } else {
          this.panAndZoom.disablePan();
        }
      },
      'cell:pointerdown': (cellView) => {
        const model = cellView.model.attributes;
        // console.log('cell:pointerdown', cellView);
        this.panAndZoom.disablePan();
      },
      // register context menu
      'cell:contextmenu': (cellView: dia.CellView) => {
        if (this.isViewerMode()) {
          return;
        }
        const id = cellView.model.attributes.id;
        if (Links.isSemanticLink(cellView)) {
          const link = this.combinedModel.semanticModel.edges.find(edge => edge.uuid === id);
          this.contextMenu.open(cellView.el, PlsRelationConceptMenuComponent, this.viewContainerRef, {
            relation: link
          });
        }
        if (Nodes.isClass(cellView)) {
          const node = this.findSemanticNodeById(id);
          this.unhighlightAll();
          cellView.highlight();
          const instance = (node as Class).instance;
          this.contextMenu.open(cellView.el, PlsSemanticClassMenuComponent, this.viewContainerRef, {
            node,
            syntaxNode: node.mapped
          });
        } else if (Nodes.isNamedEntity(cellView)) {
          const node = this.findSemanticNodeById(id);
          this.unhighlightAll();
          cellView.highlight();
          this.contextMenu.open(cellView.el, PlsNamedEntityMenuComponent, this.viewContainerRef, {
            node
          });
        } else if (Nodes.isLiteral(cellView)) {
          const node = this.findSemanticNodeById(id);
          this.unhighlightAll();
          cellView.highlight();
          this.contextMenu.open(cellView.el, PlsLiteralMenuComponent, this.viewContainerRef, {
            node
          });
        }
        if (Nodes.isSyntaxNode(cellView)) {
          const node = this.findSyntaxNodeById(id);
          this.unhighlightAll();
          cellView.highlight();
          this.contextMenu.open(cellView.el, PlsSemanticClassMenuComponent, this.viewContainerRef, {
            syntaxNode: node
          });
        }
      },
      'cell:highlight': (cellView, node, {type}) => {
        // console.log('highlight', cellView, node, type);
        if (type === dia.CellView.Highlighting.CONNECTING) {
          if (this.relation._class === 'ObjectProperty') {
            if (Nodes.isSemanticNode(cellView) && !Nodes.isLiteral(cellView)) {
              highlighters.stroke.add(cellView, node, this.CustomHighlighters.CONNECTION, {});
            }
          } else if (this.relation._class === 'DataProperty') {
            if (Nodes.isPrimitiveNode(cellView)) {
              highlighters.stroke.add(cellView, node, this.CustomHighlighters.CONNECTION, {});
            }
          }
        }
      },
      'cell:unhighlight': (cellView, node, {type}) => {
        highlighters.stroke.remove(cellView, this.CustomHighlighters.CONNECTION);
      },
      // init layout after first rendering
      'render:done': () => {
        // turn off this trigger
        this.paper.off('render:done');
        // if there are no coordinates set, we guess its the initial view
        const initial = !this.combinedModel.syntaxModel.nodes[0].x;
        if (initial) {
          setTimeout(() => {
            // on first view layout and center
            this.layout();
            setTimeout(() => this.center(), 500);
          }, 500);
        } else {
          this.center();
        }
        // subscribe to resize
        const size = this.paper.getComputedSize();
        this.$resize = new BehaviorSubject<{ width: number, height: number }>(size);
        this.$resize.pipe(
          // emit event only once every 500ms
          auditTime(100),
          // new and old value
          pairwise()
        ).subscribe((values) => {
            // update sizes in model
            this.panAndZoom.resize();
            // pan by half of diff to let graph remain in relative center
            const widthScale = (values[1].width - values[0].width) / 2;
            const heightScale = (values[1].height - values[0].height) / 2;
            this.panAndZoom.panBy({x: widthScale, y: heightScale});
          }
        );
        this.initializing = false;
      }
    });
  }

  /**
   * Handles entity dropped event
   * @param event dropped event
   */
  entityDropped(event): void {
    // test if valid target
    const node = this.hoveredElement ? this.getIfLeafNode(this.hoveredElement) : null;
    // create new semantic model node
    if (node) {
      const position = this.hoveredElement.model.attributes.position;
      let semanticNode;
      switch (this.modelNode._class) {
        case 'Class':
          const instance: Instance = {
            label: node.label,
          };
          semanticNode = {
            _class: this.modelNode._class,
            uri: this.modelNode.uri,
            label: this.modelNode.label,
            description: (this.modelNode as Class).description,
            x: position.x,
            y: position.y,
            syntaxLabel: node.label,
            syntaxPath: node.path.join('->'),
            syntaxNodeUuid: node.uuid,
            instance
          };
          break;
        case 'NamedEntity':
          semanticNode = {
            _class: this.modelNode._class,
            uri: this.modelNode.uri,
            label: this.modelNode.label,
            description: (this.modelNode as Class).description,
            x: position.x + 10,
            y: position.y + 10
          };
          break;
        case 'Literal':
          console.log('Should not happen!');
          break;
      }

      this.semanticModelElementsAdded.emit({entities: [semanticNode]});
    } else if (event.isPointerOverContainer) { // dropped in blank area
      const localPosition = this.paper.clientToLocalPoint(this.mousePosition.x, this.mousePosition.y);
      let semanticNode;
      switch (this.modelNode._class) {
        case 'Class':
          semanticNode = {
            _class: this.modelNode._class,
            uri: this.modelNode.uri,
            label: this.modelNode.label,
            description: (this.modelNode as Class).description,
            x: localPosition.x,
            y: localPosition.y
          };
          break;
        case 'NamedEntity':
          semanticNode = {
            _class: this.modelNode._class,
            uri: this.modelNode.uri,
            label: this.modelNode.label,
            description: (this.modelNode as Class).description,
            x: localPosition.x,
            y: localPosition.y
          };
          break;
        case 'Literal':
          console.log('Literals are not dropped!');
          break;
      }
      this.semanticModelElementsAdded.emit({entities: [semanticNode]});
    }
    // clear all even if dropped out of container
    this.modelNode = null;
    this.unhighlightAll();
  }

  /**
   * Setter for entity node
   * @param node - the new node
   */
  setSelectedModelNode(node: SemanticModelNode): void {
    this.modelNode = node;
  }

  /**
   * Track mouse position during entity modeling to find target nodes
   * @param event mouse event
   */
  @HostListener('mousemove', ['$event'])
  onMousemove(event: MouseEvent): void {
    if (this.relation) {
      this.mousePosition = {x: event.clientX, y: event.clientY};
    }
    if (this.modelNode) {
      this.mousePosition = {x: event.clientX, y: event.clientY};
    }
  }

  @HostListener('window:keyup', ['$event'])
  onKeyPressed(event: KeyboardEvent): void {
    if (event.code === 'Enter') {
      this.literalValueModalRef?.triggerOk();
    }
  }

  /**
   * Creates nodes and edges from syntax model
   * @param syntaxModel the syntax model
   */
  private createSyntaxGraph(syntaxModel: SyntaxModel): void {
    if (syntaxModel.nodes.length > 0) {
      syntaxModel.nodes.forEach(node => this.addSyntaxNode(node.uuid, node.label, Icons.getIcon(node), node._class, node.x, node.y));
      syntaxModel.edges.forEach(edge => this.addSyntaxLink(edge.from, edge.to));
    }
  }

  public isViewerMode(): boolean {
    return this.viewerMode || this.combinedModel?.finalized;
  }

  /**
   * Fill graph object with current schema data
   * @param showURI shows URIs instead of labels where possible
   */
  private createGraph(showURI: boolean = false): void {
    // remove old content
    this.paper.freeze();
    this.graph.clear();
    this.paper.updateViews();
    // create graph based on filter selection
    switch (this.$filter.getValue()) {
      case Filter.ALL:
        // filter syntax nodes
        const map = this.combinedModel.modelMappings;
        const mappedSyntaxNodes = this.combinedModel.syntaxModel.nodes
          .filter(node => this.combinedModel.modelMappings
            .some(mapping => mapping.schemaNodeUuid === node.uuid)
          );
        const hiddenSyntaxNodesIds = mappedSyntaxNodes.map(node => node.uuid);
        const reducedSyntaxModel = {
          root: this.combinedModel.syntaxModel.root,
          nodes: this.combinedModel.syntaxModel.nodes.filter(node => !mappedSyntaxNodes.includes(node)),
          edges: map.map(mapping => {
            return {
              from: mapping.parentUuid,
              to: mapping.semanticNodeUuid
            };
          }).concat(
            this.combinedModel.syntaxModel.edges
              .filter(edge => !hiddenSyntaxNodesIds.includes(edge.from) && !hiddenSyntaxNodesIds.includes(edge.to))
              .map(edge => {
                return {
                  from: edge.from,
                  to: edge.to
                };
              })
          )
        };
        this.createSyntaxGraph(reducedSyntaxModel);

        // show semantic nodes and links
        this.createSemanticGraph(this.combinedModel.semanticModel, showURI);
        break;
      case Filter.SYNTAX:
        // show syntax nodes and links
        this.createSyntaxGraph(this.combinedModel.syntaxModel);
        break;
      case Filter.SEMANTIC:
        // show semantic nodes and links
        this.createSemanticGraph(this.combinedModel.semanticModel, showURI);
        break;
      default:
        console.error('Unsupported Filter');
    }
    this.paper.unfreeze();
    // layout links
    this.graph.getLinks().forEach(el => Links.adjustVertices(this.graph, el));
  }

  /**
   * Add syntax node to graph
   */
  private addSyntaxNode(id: string, label: string, icon: string, subtype: string, x?: number, y?: number): void {
    const dim = this.calcDimensionsForLabel(label, !!icon);
    const position = {x, y};
    const node = Nodes.createSyntaxNode(id, label, icon, position, dim[0], dim[1], subtype);
    this.graph.addCell(node);
  }

  /**
   * Add syntax link to graph
   * @param source of link
   * @param target of link
   */
  private addSyntaxLink(source: string, target: string): void {
    const link = Links.createSyntaxLink(source, target);
    this.graph.addCell(link);
  }

  /**
   * Calculate rendered dimensions for given text and icon
   * @param text to test
   * @param icon true if also includes icon
   * @returns [width, height]
   */
  private calcDimensionsForLabel(text: string, icon: boolean): [number, number] {
    const letterSize = 7;
    const iconWidth = icon ? 30 : 0;
    const width = this.getTextWidth(text, '11pt Roboto') + 10 + iconWidth;
    const height = 2 * ((text.split('\n').length + 1) * letterSize);
    return [width, height];
  }

  /**
   * Renders text in canvas and returns rendered width
   * @param text to test
   * @param font font of text
   */
  private getTextWidth(text, font): number {
    // re-use canvas object for better performance
    const context = this._canvas.getContext('2d');
    context.font = font;
    const metrics = context.measureText(text);
    return metrics.width;
  }

  /**
   * Calculates the total dimensions for a graph element
   * @param header The header label to render
   * @param body The body label to render
   * @param icon True if an icon should be considered
   */
  private calcDimensionsForElement(header: string, body: string, icon: boolean): [number, number] {
    const headerDims = header ? this.calcDimensionsForLabel(header, false) : [0, 0];
    const bodyDims = body ? this.calcDimensionsForLabel(body, icon) : [0, 0];
    return [Math.max(headerDims[0], bodyDims[0]), headerDims[1] + bodyDims[1]];
  }

  /**
   * Creates nodes and edges from semantic model
   * @param semanticModel the semantic model
   * @param showURI maps labels to URIs where possible
   */
  private createSemanticGraph(semanticModel: SemanticModel, showURI: boolean): void {
    if (semanticModel.nodes.length > 0) {
      semanticModel.nodes.forEach(node => {
        if (node._class === 'Class') {
          // get icon from syntax node
          let icon = '';
          if ((node as Class).syntaxNodeUuid) {
            icon = Icons.getIcon(this.findSyntaxNodeById((node as Class).syntaxNodeUuid));
          }
          this.addClassNode(node.uuid, showURI ? node.uri : node.label, node.x, node.y, 1, (node as Class).instance?.label, icon);
        } else if (node._class === 'NamedEntity') {
          this.addNamedEntityNode(node.uuid, showURI ? node.uri : node.label, node.x, node.y);
        } else if (node._class === 'Literal') {
          this.addLiteralNode(node.uuid, node.label, 'text_fields', node.x, node.y);
        }
      });

      semanticModel.edges.forEach(edge => {
        if (edge._class === 'DataProperty') {
          this.addDataPropertyLink(edge.uuid, edge.from, edge.to, showURI ? edge.uri : edge.label);
        } else {
          this.addObjectPropertyLink(edge.uuid, edge.from, edge.to, showURI ? edge.uri : edge.label);
        }
      });
    }
  }

  /**
   * Lay out graph and persist positions
   */
  private layout(): void {
    // layout top to bottom with spaces: node 50, edge 50, rank (layer) 70
    layout.DirectedGraph.layout(this.graph, {
      dagre,
      graphlib,
      nodeSep: 50,
      edgeSep: 50,
      rankSep: 70,
      rankDir: 'TB'
    });
    // wait short time for rendering to complete
    setTimeout(() => {
      // update x and y values in model
      this.graph.getElements().forEach((element) => {
        this.updatePosition(element, false);
      });
      // update links layout
      setTimeout(() => {
        this.graph.getLinks().forEach(el => Links.adjustVertices(this.graph, el));
      }, 300);
      // persist new positions
      this.graphLaidOut.emit(this.combinedModel);
    }, 500);
  }

  /**
   * Fit and center graph
   */
  private center(): void {
    // update bounding box in case user dragged stuff
    this.panAndZoom.updateBBox();
    // fit and center
    this.panAndZoom.fit();
    this.panAndZoom.center();
    // leave small margin
    this.panAndZoom.zoomBy(0.98);
  }

  /**
   * Add object property link to graph
   */
  private addObjectPropertyLink(id: string, source: string, target: string, label: string, opacity?: number): dia.Link {
    const link = Links.createObjectPropertyLink(id, source, target, label, opacity);
    this.graph.addCell(link);
    return link;
  }

  /**
   * Add data property link to graph
   */
  private addDataPropertyLink(id: string, source: string, target: string, label: string, opacity?: number): dia.Link {
    const link = Links.createDataPropertyLink(id, source, target, label, opacity);
    this.graph.addCell(link);
    return link;
  }

  private createConfig(title: string, content: any, data: SyntaxNode | PlsSemanticNodeDetailsData): ModalOptions {
    const rect: DOMRect = this.viewContainerRef.element.nativeElement.getBoundingClientRect();
    return Object.assign({
      nzStyle: {position: 'absolute', top: `${rect.top + 15}px`, right: `15px`},
      nzTitle: title,
      nzContent: content,
      nzComponentParams: {
        data
      }
    }, ModalMouseEnabledConfig);
  }

  /**
   * Closes open dialog
   */
  private closeDialog(): void {
    if (this.nodeDialogRef) {
      this.nodeDialogRef.close();
      this.nodeDialogRef = null;
    }
  }

  /**
   * Closes dialog and removes all highlights from graph
   */
  private unhighlightAll(): void {
    this.closeDialog();
    this.paper.findViewsInArea(this.paper.getArea()).forEach(cell => {
      cell.unhighlight();
    });
  }

  /**
   * Adds a semantic class node to the graph
   */
  private addClassNode(id: string, uri: string, x?: number, y?: number, opacity?: number, instancelabel?: string, icon?: string): dia.Element {
    const node = this.buildClassNode(id, uri, x, y, opacity, instancelabel, icon);
    this.graph.addCell(node);
    return node;
  }

  /**
   * Builds a semantic class node
   */
  private buildClassNode(id: string, uri: string, x?: number, y?: number, opacity?: number, instancelabel?: string, icon?: string): dia.Element {
    const classDims = this.calcDimensionsForLabel(uri, false);
    let instanceDims = [0, 0];
    if (instancelabel) {
      instanceDims = this.calcDimensionsForLabel(instancelabel, !!icon);
    }
    const dim = [Math.max(classDims[0], instanceDims[0]), classDims[1] + instanceDims[1]];
    const position = {x, y};
    let node;
    if (instancelabel) {
      node = Nodes.createInstancedSemanticClassNode(id, uri, instancelabel, icon, position, dim[0], dim[1], opacity);
    } else {
      node = Nodes.createSemanticClassNode(id, uri, position, dim[0], dim[1], opacity);
    }
    return node;
  }

  /**
   * Adds a named entity node to the graph
   */
  private addNamedEntityNode(id: string, label: string, x?: number, y?: number, opacity?: number): dia.Element {
    const classDims = this.calcDimensionsForLabel(label, false);
    const dim = [classDims[0], classDims[1]];
    const position = {x, y};
    const node = Nodes.createNamedEntityNode(id, label, position, dim[0], dim[1], opacity);
    this.graph.addCell(node);
    return node;
  }

  /**
   * Adds a literal node to the graph
   */
  private addLiteralNode(id: string, value: string, icon: string, x?: number, y?: number, opacity?: number): dia.Element {
    const classDims = this.calcDimensionsForLabel(value, !!icon);
    const dim = [classDims[0], classDims[1]];
    const position = {x: x - (classDims[0] / 2), y: y - (classDims[1] / 2)};
    const node = Nodes.createLiteralNode(id, value, icon, position, dim[0], dim[1], opacity);
    this.graph.addCell(node);
    return node;
  }

  /**
   * Finds syntax node data model for given id
   * @param id of node to find
   */
  private findSyntaxNodeById(id: string): SyntaxNode {
    return this.combinedModel.syntaxModel.nodes.find(node => node.uuid === id);
  }

  /**
   * Finds semantic node data model for given id
   * @param id of node to find
   */
  private findSemanticNodeById(id: string): SemanticNode {
    return this.combinedModel.semanticModel.nodes.find(node => node.uuid === id);
  }

  /**
   * Creates nodes and edges from recommendation and adds it to global variable
   * @param recommendation the recommendation
   */
  private showRecommendation(recommendation: DeltaModification): void {
    const opacity = 0.8;
    recommendation.entities.forEach(node => {
      let icon = '';
      if (node._class === 'Class') {
        const instance = (node as Class).instance;
        if ((node as Class).syntaxNodeUuid) {
          icon = Icons.getIcon(this.findSyntaxNodeById((node as Class).syntaxNodeUuid));
        }
        this.recommendations.push(this.addClassNode(node.uuid, node.uri, node.x, node.y, opacity, instance?.label, icon));
      }
    });
    recommendation.relations.forEach(edge => this.recommendations.push(this.addObjectPropertyLink(edge.uuid, edge.from, edge.to, edge.label, opacity)));
  }

  /**
   * Searches a syntax node for gi
   * @param elementView search element
   */
  private getIfLeafNode(elementView: dia.ElementView): PrimitiveNode {
    if (Nodes.isSyntaxNode(elementView)) {
      const node = this.findSyntaxNodeById(elementView.model.id.toString());
      if (node._class === 'PrimitiveNode') {
        return node;
      }
    }
    return null;
  }

  /**
   * Update positions in data model of element and emit event
   * @param element changed element
   * @param emitEvent - true if emit should be emitted
   */
  private updatePosition(element: dia.Element, emitEvent: boolean): void {
    const model = element.attributes;
    if (Nodes.isSyntaxNode(element)) {
      // update positions in data model
      const node = this.findSyntaxNodeById(model.id);
      const position = model.position;
      if (node && (node.x !== position.x || node.y !== position.y)) {
        node.x = position.x;
        node.y = position.y;
        // emit event
        if (emitEvent) {
          const nodeChanges = new NodeChangedEvent(Nodes.syntaxNodeName, node);
          this.nodePositionChanged.emit(nodeChanges);
        }
      }
    }
    if (Nodes.isSemanticNode(element)) {
      // update positions in data model
      const node = this.findSemanticNodeById(model.id);
      const position = model.position;
      if (node && (node.x !== position.x || node.y !== position.y)) {
        node.x = position.x;
        node.y = position.y;
        // emit event
        if (emitEvent) {
          const nodeChanges = new NodeChangedEvent(Nodes.semanticClassName, node);
          this.nodePositionChanged.emit(nodeChanges);
        }
        // update mapped syntax node position
        if (node._class === 'Class' && (node as Class).syntaxNodeUuid) {
          // TODO this could be done by the backend
          const mappedNode = this.findSyntaxNodeById((node as Class).syntaxNodeUuid);
          mappedNode.x = position.x;
          mappedNode.y = position.y;
          if (emitEvent) {
            const nodeChanges = new NodeChangedEvent(Nodes.syntaxNodeName, mappedNode);
            this.nodePositionChanged.emit(nodeChanges);
          }
        }
      }
    }
  }

  /**
   * Listen to window resize events
   */
  @HostListener('window:resize')
  onResize(): void {
    if (this.$resize) {
      this.$resize.next(this.paper.getComputedSize());
    }
  }

  /**
   * Show details dialog for node with given id
   * @param id of details
   * @param type of node
   */
  private openDetails(id: string, type: NodeType): void {
    this.closeDialog();
    let nodeDialogRef: PlsSemanticNodeDetailsComponent;
    switch (type) {
      case Nodes.syntaxNodeName:
        const syntaxNode = this.findSyntaxNodeById(id);
        this.nodeDialogRef = this.modal.create(this.createConfig(syntaxNode.label, PlsSyntaxNodeDetailsComponent, syntaxNode));
        this.nodeDialogRef.afterClose.subscribe((res) => {
          if (res) {
            this.nodeDialogRef = null;
          }
        });
        break;
      case Nodes.extendedSemanticClassName:
      case Nodes.semanticClassName:
        const semanticNode = this.findSemanticNodeById(id);
        if (!semanticNode) {
          return;
        }
        const clazz: Class = semanticNode as Class;
        let examples: string[] = [];
        if (clazz.syntaxNodeUuid) {
          const synNode = this.findSyntaxNodeById(clazz.syntaxNodeUuid);
          if (synNode?._class === 'PrimitiveNode') {
            examples = (synNode as PrimitiveNode).examples;
          }
        }
        const title = `${semanticNode.label} (${semanticNode.uri})`;
        this.nodeDialogRef = this.modal.create(this.createConfig(title, PlsSemanticNodeDetailsComponent, {
          modelId: this.modelId,
          node: semanticNode,
          recommendations: this.filterRecommendationsForAnchor(semanticNode.uuid),
          examples,
          finalized: this.isViewerMode()
        }));
        nodeDialogRef = this.nodeDialogRef.componentInstance as PlsSemanticNodeDetailsComponent;
        nodeDialogRef.recommendationSelect.subscribe(recommendation => {
          this.hideRecommendations();
          if (recommendation !== null) {
            this.showRecommendation(recommendation);
          }
        });
        nodeDialogRef.recommendationAccept.subscribe(recommendation => {
          this.hideRecommendations();
          this.recommendationAccepted.emit(recommendation);
        });
        nodeDialogRef.nodeEdited.subscribe((node: SemanticModelNode) => {
          const element = this.graph.getElements().find(el => el.attributes.id === id);
          let icon;
          if ((node as Class).syntaxNodeUuid) {
            icon = Icons.getIcon(this.findSyntaxNodeById((node as Class).syntaxNodeUuid));
          }
          const newNode = this.buildClassNode(node.uuid, this.$URIMode.value ? node.uri : node.label, node.x, node.y, 1, (node as Class).instance?.label, icon);
          let links = this.graph.getLinks();
          links = links.filter(link => link.getTargetElement() === element || link.getSourceElement() === element);
          this.graph.removeCells([element]);
          this.graph.addCell(newNode);
          this.graph.addCell(links);
        });
        break;
      case Nodes.namedEntityName:
        const namedEntityNode = this.findSemanticNodeById(id);
        const namedEntityTitle = `${namedEntityNode.label} (${namedEntityNode.uri})`;
        this.nodeDialogRef = this.modal.create(this.createConfig(namedEntityTitle, PlsSemanticNodeDetailsComponent, {
          modelId: this.modelId,
          node: namedEntityNode,
          recommendations: this.filterRecommendationsForAnchor(namedEntityNode.uuid),
          examples: [],
          finalized: this.isViewerMode()
        }));
        nodeDialogRef = this.nodeDialogRef.componentInstance as PlsSemanticNodeDetailsComponent;
        break;
      case Nodes.literalName:
        const literalNode = this.findSemanticNodeById(id);
        const literalTitle = `${literalNode.label}`;
        const literal: Literal = literalNode as Literal;
        let exampleValues: string[] = [];
        if (literal.syntaxNodeUuid) {
          const synNode = this.findSyntaxNodeById(literal.syntaxNodeUuid);
          if (synNode?._class === 'PrimitiveNode') {
            exampleValues = (synNode as PrimitiveNode).examples;
          }
        }
        this.nodeDialogRef = this.modal.create(this.createConfig(literalTitle, PlsSemanticNodeDetailsComponent, {
          modelId: this.modelId,
          node: literalNode,
          recommendations: this.filterRecommendationsForAnchor(literalNode.uuid),
          examples: exampleValues,
          finalized: this.isViewerMode()
        }));
        nodeDialogRef = this.nodeDialogRef.componentInstance as PlsSemanticNodeDetailsComponent;
        nodeDialogRef.nodeEdited.subscribe((node: SemanticModelNode) => {
          const element = this.graph.getElements().find(el => el.attributes.id === id);
          const dims = this.calcDimensionsForLabel(node.label, true);
          element.attributes.size = {width: dims[0], height: dims[1]};
          element.attr('label/text', node.label);
        });
        break;
      default:
        console.error('Unsupported node type');
    }
  }

  /* RECOMMENDATIONS */
  /**
   * Filters the given list of recommendations to those which have the anchorId set as an anchor.
   * @param anchorId The id to check
   * @return Sublist of matching recommendations for the given node. Empty if none match
   */
  filterRecommendationsForAnchor(anchorId: string): DeltaModification[] {
    return this.combinedModel.recommendations.filter(rec => rec.anchors.includes(anchorId));
  }

  /**
   * Creates nodes and edges for recommendations and adds them to global variable
   * @param recommendations the recommendations
   */
  private showRecommendations(recommendations: DeltaModification[]): void {
    recommendations.forEach(rec => this.showRecommendation(rec));
  }

  /**
   * Removes recommendations from graph
   */
  private hideRecommendations(): void {
    this.graph.removeCells(this.recommendations);
    this.recommendations = [];
  }

}
