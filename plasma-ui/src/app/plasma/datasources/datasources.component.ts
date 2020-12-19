import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {DataSourceControllerService} from '../../../../build/openapi/api/dataSourceController.service';
import {CreateDataSourceDTO} from '../../../../build/openapi';
import {DataSourceDTO} from '../../../../build/openapi';
import {MatTableDataSource} from '@angular/material/table';
import {MatSort} from '@angular/material';
import {NgbModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';



@Component({
  selector: 'app-datasources',
  templateUrl: './datasources.component.html',
  styleUrls: ['./datasources.component.scss']
})
export class DatasourcesComponent implements OnInit {

  newTitle = '';
  newDescription = '';
  newLongDescription = '';

  sourceCreated = false;

  allDataSources: DataSourceDTO[] = [];
  selectedDataSource: DataSourceDTO;
  displayedColumns: string[] = ['title', 'description', 'uuid', 'operations'];

  dataSources = new MatTableDataSource(this.allDataSources);

  @ViewChild(MatSort, {static: true}) sort: MatSort;

  constructor(private http: HttpClient,
              private readonly dataSourceController: DataSourceControllerService,
              private modalService: NgbModal) {
    this.dataSourceController.getAllDataSources().subscribe(
      (datasource) => {
        this.allDataSources = datasource;
        this.dataSources = new MatTableDataSource(this.allDataSources);
        this.dataSources.sort = this.sort;
      });
  }

  @ViewChild('longDescriptionModal', {static: true})
  public longDescriptionModal: ElementRef;

  private longDescriptionModalNgb: NgbModalRef;

  ngOnInit() {
    this.dataSources.sort = this.sort;
  }


  async addDataSource() {

    // tslint:disable-next-line:max-line-length
    const createDto: CreateDataSourceDTO = {title: this.newTitle, description: this.newDescription, longDescription: this.newLongDescription};

    this.dataSourceController.createDataSource(createDto)
      .subscribe(async (res: DataSourceDTO) => {
        if ('uuid' in res) {
          this.newDescription = '';
          this.newTitle = '';
          this.newLongDescription = '';
          this.sourceCreated = true;
        }
        this.allDataSources =  await this.dataSourceController.getAllDataSources().toPromise();
        this.dataSources = new MatTableDataSource(this.allDataSources);
        this.dataSources.sort = this.sort;
        });
  }

  async deleteDataSource(dataSource: DataSourceDTO) {
    await this.dataSourceController.deleteDataSource(dataSource.uuid).toPromise();
    this.allDataSources =  await this.dataSourceController.getAllDataSources().toPromise();
    this.dataSources = new MatTableDataSource(this.allDataSources);
    this.dataSources.sort = this.sort;
  }

  displayLongDescription(dataSource: DataSourceDTO) {
    this.selectedDataSource = dataSource;
    this.longDescriptionModalNgb = this.modalService.open(this.longDescriptionModal, {windowClass: 'hugeModal'});
    this.longDescriptionModalNgb.result.then(async () => {
      return;
    }, () => {
      return;
    });
  }


}
