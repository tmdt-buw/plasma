import * as _ from 'lodash';

export interface Positioned {
  x: number;
  y: number;
}

export class GraphUtils {

  public static formatNothing(nodes: Array<Array<{ id: string } & Positioned>>) {
    const returnbuffer: Array<Array<{ id: string } & Positioned>> = [];
    for (let i = 0; i < nodes.length; i++) {
      returnbuffer[i] = nodes[i].map((node) => {
        if (node.x !== null && node.y !== null) {
          return Object.assign({}, node);
        }
        return Object.assign({}, node, {
          x: null,
          y: null
        });
      });

    }
    return _.flatten(returnbuffer);
  }

  public static formatHierarchical(nodes: Array<Array<{ id: string } & Positioned>>,
                                   edges: Array<Array<{ from: string, to: string }>>) {
    const returnbuffer: Array<Array<{ id: string } & Positioned>> = [];
    for (let i = 0; i < nodes.length; i++) {
      returnbuffer[i] = nodes[i].map((node) => {
        if (node.x !== null && node.y !== null) {
          return Object.assign({}, node);
        }
        const layer = GraphUtils.getLayerOfNode(node.id, _.flatten(edges));
        return Object.assign({}, node, {
          x: Math.random() * 1000,
          y: layer * 200
        });
      });

    }
    return _.flatten(returnbuffer);
  }

  private static getLayerOfNode<E extends { from: string, to: string }>(id: string, edges: E[]) {
    let searchStack: Array<{ layer: number, id: string }> = [{layer: 0, id}];
    let maxLayer = 0;
    while (searchStack.length > 0) {
      const stackCopy = searchStack.slice();
      searchStack = [];

      for (const node of stackCopy) {
        if (node.layer > maxLayer) {
          maxLayer = node.layer;
        }
        for (const edge of edges) {
          if (edge.to === node.id) {
            searchStack.push({layer: node.layer + 1, id: edge.from});
          }
        }
      }
    }
    return maxLayer;
  }
}
