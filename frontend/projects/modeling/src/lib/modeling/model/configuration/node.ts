import { dia, shapes } from 'jointjs';
import {
  CollisionSchema,
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
  | 'custom.ExtendedSemanticClassNode'
  | 'custom.HighlightedSemanticClassNode'
  | 'custom.HighlightedExtendedSemanticClassNode'
  | 'custom.HighlightedLiteralNode'
  | 'custom.HighlightedNamedEntityNode';
export type SyntaxNode = SchemaNode | CollisionSchema  | ObjectNode | PrimitiveNode | SetNode;
export type SemanticNode = SemanticModelNode;

/**
 * Abstract class that provides configs for nodes
 */
export abstract class Nodes {

  // internal node type names
  static readonly syntaxNodeName: NodeType = 'custom.SyntaxNode';
  static readonly classNodeName: NodeType = 'custom.SemanticClassNode';
  static readonly classNodeHighlightedName: NodeType = 'custom.HighlightedSemanticClassNode';
  static readonly extendedClassNodeName: NodeType = 'custom.ExtendedSemanticClassNode';
  static readonly extendedClassNodeHighlightedName: NodeType = 'custom.HighlightedExtendedSemanticClassNode';
  static readonly namedEntityName: NodeType = 'custom.NamedEntityNode';
  static readonly namedEntityHighlightedName: NodeType = 'custom.HighlightedNamedEntityNode';
  static readonly literalName: NodeType = 'custom.LiteralNode';
  static readonly literalHighlightedName: NodeType = 'custom.HighlightedLiteralNode';

  // extended semantic class node config
  private static classNode: dia.Cell.Constructor<dia.Element> = shapes.standard.Rectangle.define(
    Nodes.classNodeName, {
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
          cursor: 'pointer',
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
    Nodes.extendedClassNodeName, {
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
          cursor: 'pointer',
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
          cursor: 'pointer',
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
          cursor: 'pointer',
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
          cursor: 'pointer',
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
          strokeDasharray: '10,2',
          cursor: 'pointer'
        },
        icon: {
          textVerticalAnchor: 'middle',
          textAnchor: 'left',
          refX: '5',
          refY: '65%',
          fontSize: 24,
          'font-family': 'Material Icons',
          pointerEvents: 'none'
        },
        label: {
          textVerticalAnchor: 'middle',
          textAnchor: 'left',
          refX: '35',
          refY: '50%',
          fontSize: 14,
          'font-family': 'Roboto',
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
    return [Nodes.classNodeName, Nodes.extendedClassNodeName].includes(model?.attributes?.type);
  }

  /**
   * Returns true if input node is a named entity
   * @param node to test
   */
  static isNamedEntity(node: dia.CellView | dia.ElementView | dia.Element): boolean {
    const model = node instanceof dia.Element ? node : node.model;
    return Nodes.namedEntityName === model?.attributes?.type;
  }

  /**
   * Returns true if input node is a literal
   * @param node to test
   */
  static isLiteral(node: dia.CellView | dia.ElementView | dia.Element): boolean {
    const model = node instanceof dia.Element ? node : node.model;
    return Nodes.literalName === model?.attributes?.type;
  }

  /**
   * Calls constructor and returns instance of a new semantic class node
   */
  static createSemanticClassNode(id: string, header: string, position: { x: number, y: number }, width: number, height: number, opacity?: number, highlighted?: boolean): dia.Element {
    const node = new Nodes.classNode({
      id,
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
    if (highlighted) {
      node.attributes.type = Nodes.classNodeHighlightedName;
    }
    return node;
  }

  /**
   * Calls constructor and returns instance of a new semantic class node
   */
  static createInstancedSemanticClassNode(id: string, header: string, instancelabel: string, icon: string, position: { x: number, y: number }, width: number, height: number, opacity?: number, highlighted?: boolean): dia.Element {
    const node = new Nodes.extendedClassNode({
      id,
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
    if (highlighted) {
      node.attributes.type = Nodes.extendedClassNodeHighlightedName;
    }
    return node;
  }

  /**
   * Calls constructor and returns instance of a new named entity node
   */
  static createNamedEntityNode(id: string, label: string, position: { x: number, y: number }, width: number, height: number, opacity?: number, highlighted?: boolean): dia.Element {
    const node = new Nodes.namedEntityNode({
      id,
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
    return node;
    if (highlighted) {
      node.attributes.type = Nodes.namedEntityHighlightedName;
    }
  }

  /**
   * Calls constructor and returns instance of a new literal node
   */
  static createLiteralNode(id: string, label: string, icon: string, position: { x: number, y: number }, width: number, height: number, opacity?: number, highlighted?: boolean): dia.Element {
    const node = new Nodes.literalNode({
      id,
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
    if (highlighted) {
      node.attributes.type = Nodes.literalHighlightedName;
    }
    return node;
  }

  /**
   * Calls constructor and returns instance of a new syntax node
   */
  static createSyntaxNode(id: string, label: string, icon: string, position: { x: number, y: number }, width: number, height: number, subtype: string, disabled: boolean): dia.Element {
    return new Nodes.syntaxNode({
      id,
      subtype,
      position,
      size: {
        width,
        height
      },
      attrs: {
        body: {
          opacity: disabled ? 0.1 : 1
        },
        icon: {
          text: icon,
          opacity: disabled ? 0.1 : 1
        },
        label: {
          text: label,
          opacity: disabled ? 0.1 : 1
        }
      }
    });
  }
}
