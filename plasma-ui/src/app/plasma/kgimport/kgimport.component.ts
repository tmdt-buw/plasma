import { Component, OnInit } from '@angular/core';
import {RuleProvisionDTO} from '../../../../build/openapi';
import {UniversalKnowledgeGraphControllerService} from '../../../../build/openapi/api/universalKnowledgeGraphController.service';

@Component({
  selector: 'app-kgimport',
  templateUrl: './kgimport.component.html',
  styleUrls: ['./kgimport.component.scss']
})
export class KgimportComponent implements OnInit {

  ontology: string;

  constructor(private readonly universalKnowledgeGraphController: UniversalKnowledgeGraphControllerService) { }

  ngOnInit() {
  }

  public onFileChanged(event) {
    const reader = new FileReader();
    reader.readAsText(event.target.files[0], 'UTF-8');
    reader.onload = () => {
      if (typeof reader.result === 'string') {
        this.ontology = reader.result;
      }
    };
    reader.onerror = (error) => {
      console.log(error);
    };
  }

  public async uploadOntology() {
    await this.universalKnowledgeGraphController.importRdf(this.ontology).toPromise();
  }
}
