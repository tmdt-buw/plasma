import { dia, shapes } from 'jointjs';
import {
  CollisionSchema,
  CompositeNode,
  ObjectNode,
  PrimitiveNode,
  SchemaNode,
  SemanticModelNode,
  SetNode
} from '../../../api/generated/dms';


/**
 * Node type defs
 */
export type NodeType =
  'custom.SyntaxNode'
  | 'custom.SemanticClassNode'
  | 'custom.NamedEntityNode'
  | 'custom.LiteralNode'
  | 'custom.ExtendedSemanticClassNode';
export type SyntaxNode = SchemaNode | CollisionSchema | CompositeNode | ObjectNode | PrimitiveNode | SetNode;
export type SemanticNode = SemanticModelNode;

/**
 * Abstract class that provides configs for nodes
 */
export abstract class Nodes {

  // internal node type names
  static readonly syntaxNodeName: NodeType = 'custom.SyntaxNode';
  static readonly semanticClassName: NodeType = 'custom.SemanticClassNode';
  static readonly extendedSemanticClassName: NodeType = 'custom.ExtendedSemanticClassNode';
  static readonly namedEntityName: NodeType = 'custom.NamedEntityNode';
  static readonly literalName: NodeType = 'custom.LiteralNode';

  // extended semantic class node config
  private static basicClassNode: dia.Cell.Constructor<dia.Element> = shapes.standard.Rectangle.define(
    Nodes.semanticClassName, {
      attrs: {
        body: {
          rx: 5,
          ry: 5,
          strokeWidth: 2,
          magnet: true
        },
        bodyText: {
          textVerticalAnchor: 'middle',
          textAnchor: 'middle',
          refX: '50%',
          refY: 15,
          fontSize: 14,
          'font-family': 'Roboto',
          cursor: 'default',
          pointerEvents: 'none'
        }
      }
    }, {
      markup: [{
        tagName: 'rect',
        selector: 'body'
      }, {
        tagName: 'text',
        selector: 'bodyText'
      }]
    }
  );


// extended semantic class node config
  private static extendedClassNode: dia.Cell.Constructor<dia.Element> = shapes.standard.HeaderedRectangle.define(
    Nodes.extendedSemanticClassName, {
      attrs: {
        body: {
          rx: 5,
          ry: 5,
          strokeWidth: 2,
          magnet: true
        },
        header: {
          rx: 5,
          ry: 5,
          pointerEvents: 'none'
        },
        headerText: {
          textVerticalAnchor: 'middle',
          textAnchor: 'middle',
          refX: '50%',
          refY: 15,
          fontSize: 14,
          'font-family': 'Roboto',
          cursor: 'default',
          pointerEvents: 'none'
        },
        bodyIcon: {
          textVerticalAnchor: 'middle',
          textAnchor: 'left',
          refX: '5',
          refY: '58%',
          refY2: 15,
          fontSize: 24,
          'font-family': 'Material Icons',
          cursor: 'default',
          pointerEvents: 'none'
        },
        bodyText: {
          textVerticalAnchor: 'middle',
          textAnchor: 'left',
          refX: '35',
          refY: '50%',
          refY2: 15,
          fontSize: 14,
          'font-family': 'Roboto',
          cursor: 'default',
          pointerEvents: 'none'
        }
      }
    }, {
      markup: [{
        tagName: 'rect',
        selector: 'body'
      }, {
        tagName: 'rect',
        selector: 'header'
      }, {
        tagName: 'text',
        selector: 'headerText'
      }, {
        tagName: 'text',
        selector: 'bodyIcon'
      }, {
        tagName: 'text',
        selector: 'bodyText'
      }]
    }
  );

  private static namedEntityNode: dia.Cell.Constructor<dia.Element> = shapes.standard.Rectangle.define(
    Nodes.namedEntityName, {
      attrs: {
        body: {
          rx: 5,
          ry: 5,
          strokeWidth: 2,
          magnet: true
        },
        label: {
          textVerticalAnchor: 'middle',
          textAnchor: 'left',
          refX: '35',
          refY: '50%',
          fontSize: 14,
          'font-family': 'Roboto',
          cursor: 'default',
          pointerEvents: 'none'
        }
      }
    }, {
      markup: [{
        tagName: 'rect',
        selector: 'body'
      }, {
        tagName: 'text',
        selector: 'label'
      }]
    }
  );

  // basic syntax node config
  private static literalNode: dia.Cell.Constructor<dia.Element> = shapes.standard.Rectangle.define(
    Nodes.literalName, {
      attrs: {
        body: {
          rx: 5,
          ry: 5,
          strokeWidth: 2,
          strokeOpacity: 1.0
        },
        icon: {
          textVerticalAnchor: 'middle',
          textAnchor: 'left',
          refX: '5',
          refY: '65%',
          fontSize: 24,
          'font-family': 'Material Icons',
          cursor: 'default',
          pointerEvents: 'none'
        },
        label: {
          textVerticalAnchor: 'middle',
          textAnchor: 'left',
          refX: '35',
          refY: '50%',
          fontSize: 14,
          'font-family': 'Roboto',
          cursor: 'default',
          pointerEvents: 'none'
        }
      }
    }, {
      markup: [{
        tagName: 'rect',
        selector: 'body'
      }, {
        tagName: 'text',
        selector: 'icon'
      }, {
        tagName: 'text',
        selector: 'label'
      }]
    }
  );

  // basic syntax node config
  private static syntaxNode: dia.Cell.Constructor<dia.Element> = shapes.standard.Rectangle.define(
    Nodes.syntaxNodeName, {
      attrs: {
        body: {
          strokeWidth: 2,
          strokeOpacity: 0.2,
          strokeDasharray: '10,2'
        },
        icon: {
          textVerticalAnchor: 'middle',
          textAnchor: 'left',
          refX: '5',
          refY: '65%',
          fontSize: 24,
          'font-family': 'Material Icons',
          cursor: 'default',
          pointerEvents: 'none'
        },
        label: {
          textVerticalAnchor: 'middle',
          textAnchor: 'left',
          refX: '35',
          refY: '50%',
          fontSize: 14,
          'font-family': 'Roboto',
          cursor: 'default',
          pointerEvents: 'none'
        }
      }
    }, {
      markup: [{
        tagName: 'rect',
        selector: 'body'
      }, {
        tagName: 'text',
        selector: 'icon'
      }, {
        tagName: 'text',
        selector: 'label'
      }]
    }
  );

  /**
   * Returns true if input node is a syntax node
   * @param node to test
   */
  static isSyntaxNode(node: dia.CellView | dia.ElementView | dia.Element): boolean {
    const model = node instanceof dia.Element ? node : node.model;
    return model?.attributes?.type === Nodes.syntaxNodeName;
  }

  /**
   * Returns true if input node is a primitive node
   * @param node to test
   */
  static isPrimitiveNode(node: dia.CellView | dia.ElementView | dia.Element): boolean {
    const model = node instanceof dia.Element ? node : node.model;
    return model?.attributes?.type === Nodes.syntaxNodeName && model?.attributes.subtype === 'PrimitiveNode';
  }

  /**
   * Returns true if input node is a semantic node
   * @param node to test
   */
  static isSemanticNode(node: dia.CellView | dia.ElementView | dia.Element): boolean {
    return this.isClass(node) || this.isNamedEntity(node) || this.isLiteral(node);
  }

  /**
   * Returns true if input node is a semantic class
   * @param node to test
   */
  static isClass(node: dia.CellView | dia.ElementView | dia.Element): boolean {
    const model = node instanceof dia.Element ? node : node.model;
    return [Nodes.semanticClassName, Nodes.extendedSemanticClassName].includes(model?.attributes?.type);
  }

  /**
   * Returns true if input node is a named entity
   * @param node to test
   */
  static isNamedEntity(node: dia.CellView | dia.ElementView | dia.Element): boolean {
    const model = node instanceof dia.Element ? node : node.model;
    return Nodes.namedEntityName ===  model?.attributes?.type;
  }

  /**
   * Returns true if input node is a literal
   * @param node to test
   */
  static isLiteral(node: dia.CellView | dia.ElementView | dia.Element): boolean {
    const model = node instanceof dia.Element ? node : node.model;
    return Nodes.literalName ===  model?.attributes?.type;
  }

  /**
   * Calls constructor and returns instance of a new semantic class node
   */
  static createSemanticClassNode(id: string, header: string, position: { x: number, y: number }, width: number, height: number, opacity?: number): dia.Element {
    return new Nodes.basicClassNode({
      id,
      // @ts-ignore not in jointjs model but works this way
      position,
      size: {
        width,
        height
      },
      attrs: {
        body: {
          opacity: opacity ? opacity : 1,
        },
        bodyText: {
          // adjust text for icon
          refX: '50%',
          textAnchor: 'middle',
          text: header,
          opacity: opacity ? opacity : 1
        }
      }
    });
  }

  /**
   * Calls constructor and returns instance of a new semantic class node
   */
  static createInstancedSemanticClassNode(id: string, header: string, instancelabel: string, icon: string, position: { x: number, y: number }, width: number, height: number,  opacity?: number): dia.Element {
    return new Nodes.extendedClassNode({
      id,
      // @ts-ignore not in jointjs model but works this way
      position,
      size: {
        width,
        height
      },
      attrs: {
        body: {
          opacity: opacity ? opacity : 1
        },
        header: {
          opacity: opacity ? opacity : 1
        },
        headerText: {
          text: header,
          opacity: opacity ? opacity : 1
        },
        bodyIcon: {
          text: icon,
          opacity: opacity ? opacity : 1
        },
        bodyText: {
          // adjust text for icon
          refX: icon ? '35' : '50%',
          textAnchor: icon ? 'left' : 'middle',
          text: instancelabel,
          opacity: opacity ? opacity : 1
        }
      }
    });
  }

  /**
   * Calls constructor and returns instance of a new named entity node
   */
  static createNamedEntityNode(id: string, label: string, position: { x: number, y: number }, width: number, height: number, opacity?: number): dia.Element {
    return new Nodes.namedEntityNode({
      id,
      // @ts-ignore not in jointjs model but works this way
      position,
      size: {
        width,
        height
      },
      attrs: {
        body: {
          opacity: opacity ? opacity : 1,
        },
        label: {
          refX: '50%',
          textAnchor: 'middle',
          text: label,
          opacity: opacity ? opacity : 1
        }
      }
    });
  }

  /**
   * Calls constructor and returns instance of a new literal node
   */
  static createLiteralNode(id: string, label: string, icon: string, position: { x: number, y: number }, width: number, height: number, opacity?: number): dia.Element {
    return new Nodes.literalNode({
      id,
      // @ts-ignore not in jointjs model but works this way
      position,
      size: {
        width,
        height
      },
      attrs: {
        body: {
          opacity: opacity ? opacity : 1
        },
        icon: {
          text: icon,
        },
        label: {
          // adjust text for icon
          refX: '35',
          textAnchor: 'left',
          text: label,
          opacity: opacity ? opacity : 1
        }
      }
    });
  }

  /**
   * Calls constructor and returns instance of a new syntax node
   */
  static createSyntaxNode(id: string, label: string, icon: string, position: { x: number, y: number }, width: number, height: number, subtype: string): dia.Element {
    return new Nodes.syntaxNode({
      id,
      subtype,
      // @ts-ignore not in jointjs model but works this way
      position,
      size: {
        width,
        height
      },
      attrs: {
        body: {},
        icon: {
          text: icon,
        },
        label: {
          text: label,
        }
      }
    });
  }
}
