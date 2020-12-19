import { Component, ElementRef, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges, ViewChild } from '@angular/core';
import * as _ from 'lodash';
import * as Vis from 'vis';

export const EMPTY_SELECTION: { nodes: Vis.Node[], edges: Vis.Edge[] } = {nodes: [], edges: []};

@Component({
  selector: 'app-vis-network',
  template: '<div #container></div>'
})
export class VisNetworkComponent implements OnInit, OnDestroy, OnChanges {

  @ViewChild('container', {static: true})
  public container: ElementRef;

  @Input()
  public nodes: (Vis.DataSet<Vis.Node> | Vis.Node[]);

  @Input()
  public edges: (Vis.DataSet<Vis.Edge> | Vis.Edge[]);

  @Input()
  public options: Vis.Options;

  @Input()
  public selection?: { nodes: Vis.Node[], edges: Vis.Edge[] };

  @Output()
  public selectionChange: EventEmitter<{ nodes: Vis.Node[], edges: Vis.Edge[] }> = new EventEmitter();

  @Output()
  public click: EventEmitter<Vis.VisSelectProperties> = new EventEmitter();

  @Output()
  public doubleClick: EventEmitter<Vis.Properties> = new EventEmitter();

  @Output()
  /* tslint:disable-next-line:no-output-on-prefix-name */
  public oncontext: EventEmitter<Vis.Properties> = new EventEmitter();

  @Output()
  public hold: EventEmitter<Vis.Properties> = new EventEmitter();

  @Output()
  public release: EventEmitter<Vis.Properties> = new EventEmitter();

  @Output()
  public select: EventEmitter<Vis.Properties> = new EventEmitter();

  @Output()
  public selectNode: EventEmitter<Vis.Properties> = new EventEmitter();

  @Output()
  public selectNodeResolved: EventEmitter<Vis.Node> = new EventEmitter();

  @Output()
  public selectEdge: EventEmitter<Vis.Properties> = new EventEmitter();

  @Output()
  public deselectNode: EventEmitter<Vis.Properties> = new EventEmitter();

  @Output()
  public deselectEdge: EventEmitter<Vis.Properties> = new EventEmitter();

  @Output()
  public dragStart: EventEmitter<Vis.Properties> = new EventEmitter();

  @Output()
  public dragging: EventEmitter<Vis.Properties> = new EventEmitter();

  @Output()
  public dragEnd: EventEmitter<Vis.Properties> = new EventEmitter();

  @Output()
  public hoverNode: EventEmitter<{ node: Vis.IdType }> = new EventEmitter();

  @Output()
  public blurNode: EventEmitter<{ node: Vis.IdType }> = new EventEmitter();

  @Output()
  public hoverEdge: EventEmitter<{ edge: Vis.IdType }> = new EventEmitter();

  @Output()
  public blurEdge: EventEmitter<{ edge: Vis.IdType }> = new EventEmitter();

  @Output()
  public zoom: EventEmitter<{ direction: '+' | '-', scale: number, pointer: Vis.Position }> = new EventEmitter();

  @Output()
  public showPopup: EventEmitter<string> = new EventEmitter();

  @Output()
  public hidePopup: EventEmitter<void> = new EventEmitter();

  public network: Vis.Network;

  private enabledEvents: Vis.NetworkEvents[] = [];

  constructor(private htmlElement: ElementRef) {
  }

  public ngOnInit() {
    this.generateNetwork();
  }

  public ngOnDestroy() {
    if (this.network) {
      this.network.destroy();
    }
    this.enabledEvents = [];
  }

  public ngOnChanges(changes: SimpleChanges) {
    if (_.isNil(this.network)) {
      return;
    }

    for (const property in changes) {
      if (!changes.hasOwnProperty(property)) {
        continue;
      }

      switch (property) {
        case 'edges':
        case 'nodes':
          this.network.setData({
            edges: this.edges,
            nodes: this.nodes
          });
          break;
        case 'options':
          this.network.setOptions(this.options);
          break;
        case 'selection':
          this.network.setSelection(this.unresolveSelection(this.selection));
          break;
      }
    }
  }

  public addEdgeMode() {
    this.network.addEdgeMode();
  }

  public disableEditMode() {
    this.network.disableEditMode();
  }

  public getSelection() {
    return this.network.getSelection();
  }

  public getSelectedNodes() {
    return this.network.getSelectedNodes();
  }

  public getSelectedEdges() {
    return this.network.getSelectedEdges();
  }

  public getNodeAt(position: Vis.Position): Vis.IdType {
    return this.network.getNodeAt(position);
  }

  public getEdgeAt(position: Vis.Position): Vis.IdType {
    return this.network.getEdgeAt(position);
  }

  public canvasToDOM(coordinates: Vis.Position): Vis.Position {
    return this.network.canvasToDOM(coordinates);
  }

  public DOMToCanvas(coordinates: Vis.Position): Vis.Position {
    return this.network.DOMtoCanvas(coordinates);
  }

  public fit() {
    this.network.fit();
  }

  private getObservedEvents(): Vis.NetworkEvents[] {
    return (['click', 'doubleClick', 'oncontext', 'hold', 'release', 'select',
      'selectNode', 'selectEdge', 'deselectNode', 'deselectEdge', 'dragStart',
      'dragging', 'dragEnd', 'hoverNode', 'blurNode', 'hoverEdge', 'blurEdge',
      'zoom', 'showPopup', 'hidePopup'
    ] as Vis.NetworkEvents[])
      .filter((eventName) => {
        return (this as unknown as { [k in Vis.NetworkEvents]?: EventEmitter<any> })[eventName].observers.length > 0;
      });
  }

  private generateNetwork() {
    if (_.isNil(this.nodes) || _.isNil(this.edges) || _.isNil(this.options)) {
      throw new Error('Vis configuration incomplete');
    }

    const data: Vis.Data = {
      edges: this.edges,
      nodes: this.nodes
    };

    this.network = new Vis.Network(this.container.nativeElement, data, this.options);
    if (!_.isNil(this.selection)) {
      this.network.setSelection(this.unresolveSelection(this.selection));
    }

    this.getObservedEvents().forEach((event) => {
      const e = (this as unknown as { [k in Vis.NetworkEvents]?: EventEmitter<any> })[event];
      this.network.on(event, e.emit.bind(e));
      this.enabledEvents.push(event);
    });

    this.network.on('select', () => {
      if (!this.compareSelections(this.unresolveSelection(this.selection), this.getSelection())) {
        this.selectionChange.emit(this.resolveSelection(this.getSelection()));
      }
    });
  }

  private getNodeById(id: string | number) {
    const nodes = _.isArray(this.nodes) ? this.nodes : this.nodes.get();
    for (const node of nodes) {
      if (node.id === id) {
        return node;
      }
    }
    return null;
  }

  private getEdgeById(id: string | number) {
    const edges = _.isArray(this.edges) ? this.edges : this.edges.get();
    for (const edge of edges) {
      if (edge.id === id) {
        return edge;
      }
    }
    return null;
  }

  private resolveSelection(selection?: { nodes: Vis.IdType[], edges: Vis.IdType[] }): { nodes: Vis.Node[], edges: Vis.Edge[] } {
    if (!selection) {
      return EMPTY_SELECTION;
    }

    return {
      nodes: selection.nodes.map((nodeId) => {
        const node = this.getNodeById(nodeId);
        if (node === null) {
          throw Error(`Tried to resolve node id "${nodeId}" for non existing node`);
        }
        return node;
      }),
      edges: selection.edges.map((edgeId) => {
        const edge = this.getEdgeById(edgeId);
        if (edge === null) {
          throw Error(`Tried to resolve edge id "${edgeId}" for non existing edge`);
        }
        return edge;
      })
    };
  }

  private unresolveSelection(selection?: { nodes: Vis.Node[], edges: Vis.Edge[] }): { nodes: Vis.IdType[], edges: Vis.IdType[] } {
    if (!selection) {
      return {nodes: [], edges: []};
    }

    return {
      nodes: selection.nodes.map((node) => {
        if (node.id) {
          return node.id;
        } else {
          throw Error('Tried to unresolve node without id.');
        }
      }),
      edges: selection.edges.map((edge) => {
        if (edge.id) {
          return edge.id;
        } else {
          throw Error('Tried to unresolve node without id.');
        }
      })
    };
  }

  private compareSelections(a: { nodes: Vis.IdType[], edges: Vis.IdType[] }, b: { nodes: Vis.IdType[], edges: Vis.IdType[] }): boolean {
    if (a && !b || !a && b) {
      return false;
    }

    for (const id of a.nodes) {
      const index = b.nodes.indexOf(id);
      if (index === -1) {
        return false;
      }
    }
    for (const id of b.nodes) {
      const index = a.nodes.indexOf(id);
      if (index === -1) {
        return false;
      }
    }
    for (const id of a.edges) {
      const index = b.edges.indexOf(id);
      if (index === -1) {
        return false;
      }
    }
    for (const id of b.edges) {
      const index = a.edges.indexOf(id);
      if (index === -1) {
        return false;
      }
    }
    return true;
  }
}
