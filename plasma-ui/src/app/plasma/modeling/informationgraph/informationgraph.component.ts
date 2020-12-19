import {Component, EventEmitter, Input, NgZone, OnChanges, Output, SimpleChanges, ViewChild} from '@angular/core';
import * as _ from 'lodash';
import * as Vis from 'vis';

import 'vis/dist/vis-network.min.css';

import {FONTAWESOME_BASE64, MONTSERRAT_BASE64} from './fonts';
import {
  DataSourceSchemaDTO,
  EntityTypeDTO,
  NodeDTO,
  PrimitiveDTO,
  PrimitiveEntityTypeEdgeDTO,
  SchemaNodeDTO
} from '../../../../../build/openapi';
import {GraphUtils, Positioned} from './GraphUtils';
import {VisNetworkComponent} from '../../../../common/vis/vis-network.component';
import {CheckboxFormElementComponent} from '../../../../common/schema-form/elements/checkbox-form-element.component';

export enum SubtreeFilter {
  SemanticModel,
  SyntaxModel,
  Both
}

@Component({
  selector: 'app-informationgraph',
  templateUrl: './informationgraph.component.html',
  styleUrls: ['./informationgraph.component.scss']
})
export class InformationgraphComponent implements OnChanges {

  @Input()
  public schema: DataSourceSchemaDTO;

  @Input()
  public allowEdgeAddition = false;

  @ViewChild('network', {static: true})
  public network: VisNetworkComponent;

  public visNetworkOptions: Vis.Options;

  @Input()
  public selection?: { nodes: Vis.Node[], edges: Vis.Edge[] };

  @Input()
  public subtreeFilter: SubtreeFilter = SubtreeFilter.Both;

  @Input()
  public treeFilter: string;

  @Output()
  public selectionChange: EventEmitter<{ nodes: Vis.Node[], edges: Vis.Edge[] }> = new EventEmitter();

  @Output()
  public dragEnd: EventEmitter<Vis.Properties> = new EventEmitter();

  @Output()
  public oncontext: EventEmitter<Vis.Properties> = new EventEmitter();

  @Output()
  public selectNode: EventEmitter<Vis.Properties> = new EventEmitter();

  @Output()
  public deselectNode: EventEmitter<Vis.Properties> = new EventEmitter();

  @Output()
  public doubleclick: EventEmitter<Vis.Properties> = new EventEmitter();

  @Output()
  public addEdge: EventEmitter<Vis.Edge> = new EventEmitter();

  @Output()
  public nodePositionsChange: EventEmitter<DataSourceSchemaDTO> = new EventEmitter();

  public nodes: Vis.DataSet<Vis.Node>;
  public edges: Vis.DataSet<Vis.Edge>;

  constructor(private ngZone: NgZone) {
  }


  public ngOnChanges(changes: SimpleChanges) {
    if (!this.schema) {
      // clicking redo without undoing first, results in no schema update! just ignore it
      return;
    }
    const unformattedNodes = [];
    const unformattedEdges = [];

    if (this.schema.semanticModel) {
      unformattedNodes.push(this.schema.semanticModel.nodes.map((node) => {
        const hidden =
          (this.subtreeFilter === SubtreeFilter.SyntaxModel &&
            !this.schema.primitiveToEntityTypeList.some((edge) => edge.to === node.id))
          || (this.treeFilter ? (node.label ? !node.label.toLowerCase().includes(this.treeFilter.toLowerCase()) : false) : false);
        return Object.assign({image: this.getEntityNodeSVG(node)},
          node, {label: null, hidden});
      }));
      unformattedEdges.push(this.schema.semanticModel.edges.map((edge) => {
        return Object.assign({},
          edge, {label: edge.concept.name, color: {color: '#042042', inherit: false}});
      }));
    }

    if (this.schema.syntaxModel) {
      unformattedNodes.push(this.filterNodeList(this.schema.syntaxModel.nodes, this.schema.primitiveToEntityTypeList).map((node) => {
        return Object.assign({image: this.getSyntaxNodeSVG(node)},
          node, {
            label: null,
            hidden: this.subtreeFilter === SubtreeFilter.SemanticModel ||
              (this.treeFilter ? (node.label ? !node.label.toLowerCase().includes(this.treeFilter.toLowerCase()) : false) : false)
          });
      }));
      unformattedEdges.push(this.schema.syntaxModel.edges.map((edge) => {
        return Object.assign({},
          edge, {color: {color: '#042042', inherit: false}, dashes: true});
      }) as any);
    }

    if (this.schema.primitiveToEntityTypeList) {
      unformattedEdges.push(this.schema.primitiveToEntityTypeList.map((edge) => {
        return Object.assign({},
          edge, {label: null, color: {color: '#042042', inherit: false}});
      }) as any);
    }
    // THIS DOES NOT WORK :GraphUtils.formatHierarchical(unformattedNodes, unformattedEdges);
    const nodes: Array<{ id: string, layer?: number } & Positioned> = GraphUtils.formatNothing(unformattedNodes);
    const edges = _.union(...unformattedEdges);

    if (this.nodes) {
      window.setTimeout(() => {
        this.nodes.clear();
        this.nodes.add(nodes);
      }, 500);
    } else {
      this.nodes = new Vis.DataSet<Vis.Node>(nodes);
    }
    if (this.edges) {
      window.setTimeout(() => {
        this.edges.clear();
        this.edges.add(edges);
      }, 500);
    } else {
      this.edges = new Vis.DataSet<Vis.Edge>(edges);
    }

    // if no positions given render hierachical
    const renderHierachical = this.nodes.get().every((node) => node.x === null);
    let hierarchicalLayout;
    if (renderHierachical) {
      hierarchicalLayout = {
        hierarchical: {
          enabled: true,
          sortMethod: 'directed'
        },
        improvedLayout: false
      };
    } else {
      hierarchicalLayout = {
        hierarchical: false,
        improvedLayout: false
      };
    }

    this.visNetworkOptions = {
      nodes: {
        shape: 'image',
        shapeProperties: {
          borderDashes: false,
          borderRadius: 0,
          interpolation: false,
          useBorderWithImage: true,
          useImageSize: true
        },
        color: {
          border: 'transparent',
          background: 'transparent',
          hover: {
            background: '#adb5bd',
            border: '#042042'
          },
          highlight: {
            background: '#adb5bd',
            border: '#042042'
          }
        }
      },
      edges: {
        arrows: 'to',
        width: 2,
        arrowStrikethrough: true,
        smooth: {
          type: 'curvedCCW',
          roundness: 0.2,
          forceDirection: 'none'
        },
        chosen: false,
        font: {
          align: 'top',
          size: 20
        },
        color: {color: '#042042'}
      },
      groups: {
        task: {
          shape: 'image'
        }
      },
      layout: hierarchicalLayout,
      physics: {
        enabled: false
      },
      interaction: {
        zoomView: true,
        selectConnectedEdges: false,
        hoverConnectedEdges: false,
        hover: true
      },
      manipulation: {
        enabled: this.allowEdgeAddition,
        initiallyActive: true,
        addNode: false,
        editEdge: false,
        addEdge: (edge: Vis.Edge, callback: any) => {
          this.addEdge.emit(edge);
        }
      }
    } as any;
    if (changes.subtreeFilter === undefined || Object.keys(changes).length > 1) {
      this.nodePositionsChange.emit(this.convertToSchema(nodes));
    }
  }

  public getNetwork() {
    return this.network;
  }

  public getEntityNodeSVG(node: EntityTypeDTO) {

    const icon: string = this.getMappedPrimitiveNode(node.id) ? this.getDataTypeIcon(this.getMappedPrimitiveNode(node.id)) : null;
    const width: number = Math.max(node.concept.name.length, node.label.length) * 12 + 15 + (icon ? 45 : 0);
    const svg = // I'm going to burn in hell for this.
      `<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="65">
                <defs>
                    <linearGradient id="grad1" x1="0%" y1="0%" x2="0%" y2="100%">' +
                        <stop offset="0%" style="stop-color:#042042;stop-opacity:1" />
                        <stop offset="50%" style="stop-color:#042042;stop-opacity:1" />
                        <stop offset="50%" style="stop-color:rgb(255,255,255);stop-opacity:1" />
                        <stop offset="100%" style="stop-color:rgb(247,247,247);stop-opacity:1" />
                    </linearGradient>
                    <style type="text/css">
                        @font-face {font-family: "FontAwesome"; src: url("${FONTAWESOME_BASE64}");}
                        @font-face {font-family: "Montserrat"; src: url("${MONTSERRAT_BASE64}"); }
                    </style>
                </defs>
                <path d="M 18 3 h${width - 33} a12,12 0 0 1 12,12 v48 h-${width - 6} v-48 a12,12 0 0 1 12,-12 z"
                fill="url(#grad1)" stroke="` + '#042042' + `" stroke-width="3" />
                <text x="${Math.floor(width / 2)}" y="24" text-anchor="middle" font-family="Montserrat" fill="#ffffff" font-size="18">
                    ${node.concept.name}
                </text>
                ${icon ? `<text x="20" y="54" text-anchor="middle" font-family="FontAwesome" font-size="20">${icon}</text>` : ''}
                <text x="${Math.floor(width / 2) + (icon ? 10 : 1)}" y="54" text-anchor="middle" font-family="Montserrat" font-size="20">
                    ${node.label}
                </text>
            </svg>`;
    return 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svg);
  }


  public getSyntaxNodeSVG(node: SchemaNodeDTO) {
    const width: number = node.label.length * 12 + 50;
    const icon: string = this.getDataTypeIcon(node);
    let hasSuggestion = false;
    if ((node as PrimitiveDTO).suggestedEntityConcepts) {
      hasSuggestion = (node as PrimitiveDTO).suggestedEntityConcepts.length > 0;
    }

    const svg = // I'm going to burn in hell for this.
      `<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="40">
                <defs>
                    <linearGradient id="grad1" x1="0%" y1="0%" x2="0%" y2="100%">' +
                        <stop offset="0%" style="stop-color:rgb(255,255,255);stop-opacity:1" />
                        <stop offset="100%" style="stop-color:rgb(247,247,247);stop-opacity:1" />
                    </linearGradient>
                    <style type="text/css">
                        @font-face {font-family: "FontAwesome"; src: url("${FONTAWESOME_BASE64}");}
                        @font-face {font-family: "Montserrat"; src: url("${MONTSERRAT_BASE64}"); }
                        .label { font-family: "Montserrat" }
                    </style>
                </defs>
                <rect x="3" y="3" width="${width - 6}" height="34" stroke="#042042"
                stroke-dasharray="${hasSuggestion ? '0,0' : '6,3'}" stroke-width="3" fill="url(#grad1)" rx="12" ry="12"></rect>
                <text x="20" y="27" text-anchor="middle" font-family="FontAwesome" font-size="20">${icon}</text>

                <text x="${Math.floor(width / 2) + (icon ? 10 : 1)}" y="27"
                text-anchor="middle" font-family="Montserrat" font-size="20" color="#042042">${node.label}</text>
            </svg>`;
    return 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svg);
  }

  private getDataTypeIcon(node: NodeDTO): string {
    let icon;

    switch (node['@class']) {
      case 'ObjectNodeDTO':
        icon = '&#xf03a;';
        break;
      case 'PrimitiveDTO':
        icon = CheckboxFormElementComponent.getDataTypeIcon((node as PrimitiveDTO).dataType);
        break;
      case 'CompositeDTO':
        icon = '&#xf0c4;';
        break;
      case 'SetNodeDTO':
        icon = '&#xf0cb;';
        break;
    }

    return icon;
  }

  private convertToSchema(nodes: Array<{ id: string, layer?: number } & Positioned>): DataSourceSchemaDTO {
    return {
      semanticModel: this.schema.semanticModel ? Object.assign({}, this.schema.semanticModel, {
        nodes: this.schema.semanticModel.nodes.map((node) => {
          return Object.assign({}, node, _.pick(nodes.find((node2) => node2.id === node.id), ['x', 'y']));
        })
      }) : undefined,
      syntaxModel: this.schema.syntaxModel ? Object.assign({}, this.schema.syntaxModel, {
        nodes: this.schema.syntaxModel.nodes.map((node) => {
          return Object.assign({}, node, _.pick(nodes.find((node2) => node2.id === node.id), ['x', 'y']));
        })
      }) : undefined,
      primitiveToEntityTypeList: this.schema.primitiveToEntityTypeList
    };
  }

  private filterNodeList(allNodes: SchemaNodeDTO[],
                         primitiveToEntityTypeList: PrimitiveEntityTypeEdgeDTO[]): SchemaNodeDTO[] {
    const newList: SchemaNodeDTO[] = [];
    allNodes.forEach((syntaxModelEntry) => {
        if (!this.schema.semanticModel.nodes.find((semanticModelEntry) => {
          if (semanticModelEntry.mappedToData) {
            const primitive = primitiveToEntityTypeList.find((value) => syntaxModelEntry.id === value.mappedPrimitive);
            return primitive !== undefined && primitive.to === semanticModelEntry.id;
          }
          return false;
        })) {
          newList.push(syntaxModelEntry);
        }
      }
    );
    return newList;
  }

  private getMappedPrimitiveNode(entityNodeId: string): PrimitiveDTO {
    const mapping = this.schema.primitiveToEntityTypeList.find((p) => p.to === entityNodeId);
    if (mapping) {
      const primitive = this.schema.syntaxModel.nodes.find((p) => p.id === mapping.mappedPrimitive);
      return primitive as PrimitiveDTO;
    }
    return null;
  }

  private getOriginalLabel(node: EntityTypeDTO): string {
    const mappedPrimtive = this.getMappedPrimitiveNode(node.id);
    if (!mappedPrimtive) {
      return node.label;
    } else {
      return mappedPrimtive.label;
    }
  }

}
