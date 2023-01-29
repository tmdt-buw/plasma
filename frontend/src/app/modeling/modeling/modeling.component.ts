import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ModelingControllerService, ModelingInfo } from '@tmdt/dms';

@Component({
  selector: 'app-modeling',
  templateUrl: './modeling.component.html',
  styleUrls: ['./modeling.component.less']
})
export class ModelingComponent implements OnInit {

  modelId: string;
  model: ModelingInfo;
  finalizationEnabledByServer = undefined;

  constructor(private route: ActivatedRoute, private modelingService: ModelingControllerService, private router: Router) {
  }

  ngOnInit(): void {
    this.modelId = this.route.snapshot.paramMap.get('id');
    this.modelingService.listModelings().subscribe(modelings => this.model = modelings.find(model => model.id === this.modelId));
    if (this.finalizationEnabledByServer === undefined) {
      this.finalizationEnabledByServer = false;
      this.modelingService.finalizeModelingAvailable().subscribe(res => {
          this.finalizationEnabledByServer = res;
        },
        error => {
          console.log(error);
          this.finalizationEnabledByServer = false;
        });
    }
  }

  goToModelingsList(): void {
    this.router.navigate(['/modelings']);
  }

}
