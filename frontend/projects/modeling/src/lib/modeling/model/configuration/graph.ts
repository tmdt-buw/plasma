import { dia } from 'jointjs';
import { Links } from './link';

/**
 * Abstract class that provides configs for graphs
 */
export abstract class Graphs {

  /**
   * Call constructor for graph
   */
  static createGraph(): dia.Graph {
    const graph = new dia.Graph({
      // adjust relations verticals if something changes
      'add remove change:source change:target': (view) => {
        Links.adjustVertices(graph, view);
      }
    });
    return graph;
  }

}

