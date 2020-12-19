import {Component, OnDestroy, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {
  NodeDTO,
  SchemaAnalysisDataProvisionDTO
} from '../../../../build/openapi';
import {DataSourceDTO} from '../../../../build/openapi';
import {AnalysisControllerService} from '../../../../build/openapi/api/analysisController.service';
import {Subscription} from 'rxjs';
import {DataSourceControllerService} from '../../../../build/openapi/api/dataSourceController.service';


@Component({
  selector: 'app-datasources',
  templateUrl: './schemaanalysis.component.html',
  styleUrls: ['./schemaanalysis.component.scss']
})
export class SchemaanalysisComponent implements OnInit, OnDestroy {

  constructor(private http: HttpClient, private readonly dataSourceController: DataSourceControllerService,
              private readonly schemaanalysisController: AnalysisControllerService) {
    this.dataSourceController.getAllDataSources().subscribe(
      (datasource) => {
        this.allDataSources = datasource;
        this.allDataSources.sort((a, b) => {
          return (a.title.toUpperCase() < b.title.toUpperCase()) ? -1 : (a.title.toUpperCase() > b.title.toUpperCase()) ? 1 : 0;

        });
      });
  }

  allDataSources: DataSourceDTO[] = [];

  selectedDataSource: DataSourceDTO;


  lastDataPoints: string[] = [];

  public dataSourceInitialized: boolean;
  public currentDataPoint = '';
  public schemaAnalysisRawResult: string;
  public schemaAnalysisResult: NodeDTO;
  public schemaAnalysisReady: boolean;
  public initSubscription: Subscription;

  ngOnInit() {
  }

  ngOnDestroy() {
    if (this.initSubscription) {
      this.initSubscription.unsubscribe();
    }
  }

  public async isInitialized() {
    this.dataSourceInitialized = await this.schemaanalysisController.existing(this.selectedDataSource.uuid).toPromise();
  }

  public async isReady() {
    this.schemaAnalysisReady = await this.schemaanalysisController.isReady(this.selectedDataSource.uuid).toPromise();
  }


  public async initSchemaAnalysis() {
    this.initSubscription = this.schemaanalysisController.initAnalysis(this.selectedDataSource.uuid).subscribe(
      () => {this.dataSourceInitialized = true; }
    );
  }

  addLineSample() {
    const sendDataDto: SchemaAnalysisDataProvisionDTO = {data: this.currentDataPoint};
    this.schemaanalysisController.addDataPoint(this.selectedDataSource.uuid, sendDataDto).toPromise()
      .then(r => console.log('data points has been added'));
    this.lastDataPoints.push(this.currentDataPoint);
  }

  finish() {
    this.schemaanalysisController.finish(this.selectedDataSource.uuid)
      .subscribe(async () => {await this.isReady(); });
  }

  public async getResult() {
    this.schemaAnalysisResult = await this.schemaanalysisController
      .getResult(this.selectedDataSource.uuid).toPromise();
    this.schemaAnalysisRawResult = JSON.stringify(this.schemaAnalysisResult, undefined, 4);
  }

  public async changeDatasource() {
    this.lastDataPoints = [];
    await this.isInitialized();
    await this.isReady();
    this.currentDataPoint = '';
    this.schemaAnalysisResult = null;
    this.schemaAnalysisRawResult = '';
  }

  public isValidJson() {
    try {
      JSON.parse(this.currentDataPoint);
      return true;
    } catch (e) {
      return false;
    }
  }
}
