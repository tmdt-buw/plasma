import { Component, OnInit } from '@angular/core';
import {UniversalKnowledgeGraphControllerService} from '../../../../build/openapi/api/universalKnowledgeGraphController.service';

@Component({
  selector: 'app-kgexport',
  templateUrl: './kgexport.component.html',
  styleUrls: ['./kgexport.component.scss']
})
export class KgexportComponent implements OnInit {

  format: 'xml' | 'turtle' = 'turtle';
  flat: 'true' | 'false' = 'false';

  formatUpper: 'xml' | 'turtle' = 'turtle';

  ontology: string;
  upperOntology: string;

  constructor(private readonly universalKnowledgeGraphController: UniversalKnowledgeGraphControllerService) { }

  ngOnInit() {
  }

  public async exportRdf() {
    this.ontology = await this.universalKnowledgeGraphController.convertGraphToRdf(this.format, JSON.parse(this.flat)).toPromise();
    this.download('ontology.rdf', this.ontology);
  }

  public async getUpperOntology() {
    this.upperOntology = await this.universalKnowledgeGraphController.getUpperOntology(this.formatUpper).toPromise();
    this.download('upperOntology.rdf', this.upperOntology);
  }

  private download(filename: string, content: string) {
    const blob = new Blob([content], { type: 'application/xml' });
    const link = document.createElement('a');
    link.setAttribute('type', 'hidden');
    link.href = window.URL.createObjectURL(blob);
    link.download = filename;
    link.target = '_blank';
    document.body.appendChild(link);
    link.click();
    link.remove();
  }

}
