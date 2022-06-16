import { Component, Input, OnInit } from '@angular/core';
import { NestedTreeControl } from '@angular/cdk/tree';
import { ArrayDataSource } from '@angular/cdk/collections';

export interface JsonNode {
  key: string;
  value: any;
  type: undefined | string;
  description: string;
  children: JsonNode[];
}

@Component({
  selector: 'app-json-viewer',
  templateUrl: './json-viewer.component.html',
  styleUrls: ['./json-viewer.component.less']
})
export class JsonViewerComponent implements OnInit {

  @Input() json: any;

  treeControl = new NestedTreeControl<JsonNode>(node => node.children);
  dataSource: ArrayDataSource<JsonNode>;

  hasChild = (_: number, node: JsonNode) => !!node.children && node.children.length > 0;

  ngOnInit(): void {
    const segments = [];
    if (typeof this.json === 'object') {
      Object.keys(this.json).forEach(key => {
        segments.push(this.parseKeyValue(key, this.json[key]));
      });
    } else {
      segments.push(this.parseKeyValue(`(${typeof this.json})`, this.json));
    }
    this.dataSource = new ArrayDataSource(segments);
  }

  shortDescription(node: JsonNode): string {
    return node.description.split(' ')[0];
  }

  private parseKeyValue(key: any, value: any): JsonNode {
    const node: JsonNode = {
      key,
      value,
      type: undefined,
      description: '' + value,
      children: []
    };
    switch (typeof node.value) {
      case 'number': {
        node.type = 'number';
        break;
      }
      case 'boolean': {
        node.type = 'boolean';
        break;
      }
      case 'function': {
        node.type = 'function';
        break;
      }
      case 'string': {
        node.type = 'string';
        node.description = '"' + node.value + '"';
        break;
      }
      case 'undefined': {
        node.type = 'undefined';
        node.description = 'undefined';
        break;
      }
      case 'object': {
        if (node.value === null) {
          node.type = 'null';
          node.description = 'null';
        } else if (Array.isArray(node.value)) {
          node.type = 'array';
          node.description = 'Array(' + node.value.length + ') ' + JSON.stringify(node.value);
          node.value.forEach((nodeValue, index) => {
            node.children.push(this.parseKeyValue(index, nodeValue));
          });
        } else if (node.value instanceof Date) {
          node.type = 'date';
        } else {
          node.type = 'object';
          node.description = 'Object ' + JSON.stringify(node.value);
          Object.keys(node.value).forEach(nodeKey => {
            node.children.push(this.parseKeyValue(nodeKey, node.value[nodeKey]));
          });
        }
        break;
      }
    }
    return node;
  }
}
