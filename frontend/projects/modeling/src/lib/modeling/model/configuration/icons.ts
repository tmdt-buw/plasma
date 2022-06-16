import { CompositeNode, ObjectNode, PrimitiveNode, SetNode } from '../../../api/generated/dms';
import { SyntaxNode } from './node';

/**
 * Abstract class that provides configs for icons
 */
export abstract class Icons {

  /**
   * Returns material icon string for given node
   * @param node input
   */
  static getIcon(node: SyntaxNode): string {
    switch (node._class) {
      case 'ObjectNode':
        return 'list';
      case 'PrimitiveNode':
        const value = ((node as PrimitiveNode).datatype);
        switch (value) {
          case 'Number':
            return 'looks_one';
          case 'String':
            return 'text_fields';
          case 'Boolean':
            return 'check_box';
          case 'Binary':
            return 'attach_file';
          case 'Unknown':
            return 'not_listed_location';
          default:
            return '';
        }
      case 'CompositeNode':
        return 'content_cut';
      case 'SetNode':
        return 'format_list_numbered';
      default:
        return '';
    }
  }

}
