import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { Subscription } from 'rxjs';
import { AnalysisControllerService, SchemaAnalysisDataProvisionDTO, SyntaxModel } from '../api/generated';
import { NzNotificationService } from 'ng-zorro-antd/notification';
import { HttpStatusCode } from '@angular/common/http';

@Component({
  selector: 'app-schema-analysis',
  templateUrl: './schema-analysis.component.html',
  styleUrls: ['./schema-analysis.component.less']
})
export class SchemaAnalysisComponent implements OnInit, OnDestroy {

  @Input() dataId: string;
  @Input() usingFiles = false;
  initializing: boolean = true;
  lastDataPoints: string[] = [];

  currentDataPoint = '{"adress":{"street": "PLASMA Street", "number":"34"}, "speed": 38.56, "allowed": 30, "tolerance": 3, "exceeded": 5.56}';
  result: any;
  isReady: boolean;
  initSubscription: Subscription;

  loading;
  error;

  @Output() schemaAvailable = new EventEmitter<SyntaxModel>();
  @Output() errorOccurred = new EventEmitter<string>();
  displayErrorModal: boolean = false;
  displaySchemaModal: boolean = false;

  constructor(private schemaService: AnalysisControllerService, private notification: NzNotificationService) {
  }

  ngOnInit(): void {
    this.schemaService.existing(this.dataId).subscribe(initialized => {
      if (!initialized) {
        this.schemaService.initAnalysis(this.dataId).subscribe(() => this.initializing = false);
      } else {
        this.initializing = false;
        this.getIsReady();
      }
    }, error => {
      this.handleError(error);
      this.error = error.error.message;
    });
  }

  ngOnDestroy(): void {
    if (this.initSubscription) {
      this.initSubscription.unsubscribe();
    }
  }

  getIsReady(): void {
    this.loading = true;
    this.schemaService.isReady(this.dataId).subscribe((ready) => {
      this.isReady = ready;
      if (ready) {
        this.getResult();
      } else {
        this.loading = false;
      }
    }, error => this.handleError(error));
  }

  addLineSample(): void {
    const data = JSON.parse(this.currentDataPoint);

    if (Array.isArray(data)) {
      const sendDataDto = [];
      data.forEach(sample => {
        sendDataDto.push({data: JSON.stringify(sample)});
      });

      this.schemaService.addDataPoints(this.dataId, sendDataDto).subscribe(() => {
        sendDataDto.forEach(x => {
          this.lastDataPoints.push(x.data);
        });
      }, error => this.handleError(error));

    } else {
      this.addSample(data);
    }
  }


  addSample(sample: any): void {
    const sampleString = JSON.stringify(sample);
    const sendDataDto: SchemaAnalysisDataProvisionDTO = {data: sampleString};
    this.schemaService.addDataPoint(this.dataId, sendDataDto).subscribe(() => {
      this.lastDataPoints.push(sampleString);
    }, error => this.handleError(error));
  }

  startAnalysis(): void {
    if (!this.isReady) {
      this.loading = true;
      this.schemaService.finish(this.dataId).subscribe(() => this.getIsReady(),
        error => this.handleError(error));
    }
  }

  getResult(): void {
    this.schemaService.getResult(this.dataId).subscribe((result) => {
      this.result = result;
      this.loading = false;
      this.schemaAvailable.emit(result);
    }, error => {
      this.handleError(error);
      this.result = undefined;
    });
  }

  isValidJson(): boolean {
    try {
      JSON.parse(this.currentDataPoint);
      return true;
    } catch (e) {
      return false;
    }
  }

  displaySchema(): void {
    this.displaySchemaModal = true;
  }

  displayErrorInfo(): void {
    this.displayErrorModal = true;
  }

  closeModals(): void {
    this.displayErrorModal = false;
    this.displaySchemaModal = false;
  }

  handleError(error): void {
    if (error.status === 0){
      this.notification.error('Connection error occurred', 'This is most likely caused by a CORS error. Check the system setup.');
    }
    else if (error.status === HttpStatusCode.ServiceUnavailable){
      this.notification.error('Schema Analysis Service Not Available', 'The analysis service could not be reached. Please try again in a few minutes or contact support.');
    }
    else{
      this.notification.error('Unexpected error occurred', 'The analysis service returned:<br>' + error.statusText + ': ' + error.message);
    }
  }
}
