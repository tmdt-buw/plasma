import { dia } from 'jointjs';
import { Relation } from '../../../api/generated/dms';
import { Links } from './link';
import { Nodes } from './node';
import sorting = dia.Paper.sorting;

/**
 * Abstract class that provides configs for papers
 */
export abstract class Papers {

  /**
   * Call constructor for paper
   * @param graph model
   * @param activeRelation function
   * @param moveNodes if true, freezes elements in place
   */
  static createPaper(graph: dia.Graph, activeRelation: () => Relation, moveNodes: boolean): dia.Paper {
    const interactive = () => {
      if (!moveNodes) {
        if (activeRelation()?._class === 'ObjectProperty') {
          return {
            labelMove: true,
            addLinkFromMagnet: true,
            elementMove: false
          };
        } else if (activeRelation()?._class === 'DataProperty') {
          return {
            labelMove: true,
            addLinkFromMagnet: true,
            elementMove: false
          };
        } else {
          return {
            labelMove: true,
            addLinkFromMagnet: false,
            elementMove: true
          };
        }
      } else {
        return {
          labelMove: false,
          addLinkFromMagnet: false,
          elementMove: false
        };
      }
    };
    return new dia.Paper({
      model: graph,
      width: '100%',
      height: '100%',
      background: {
        opacity: 0
      },
      gridSize: 1,
      frozen: true,
      async: true,
      highlighting: {
        connecting: false
      },
      // disable links pinning to blank space since we only want to connect nodes with links
      linkPinning: false,
      // enable link drawing and disable move nodes if relation is selected in menu
      interactive,
      // ony allow links between semantic nodes
      allowLink: (linkView: dia.LinkView) => {
        // console.log(activeRelation(), activeRelation()._class === 'ObjectProperty' && Links.targetsSemanticNode(linkView) && !Nodes.isLiteral(linkView.model?.getTargetElement()));
        return (activeRelation()._class === 'ObjectProperty' && Links.targetsSemanticNode(linkView) && !Nodes.isLiteral(linkView.model?.getTargetElement())) ||
          (activeRelation()._class === 'DataProperty' && Nodes.isPrimitiveNode(linkView.model?.getTargetElement()));
      },
      sorting: sorting.NONE,
      // default link style
      defaultLink: () => {
        const label = activeRelation().label;
        return Links.createDefaultLink(label);
      }
    });
  }
}
