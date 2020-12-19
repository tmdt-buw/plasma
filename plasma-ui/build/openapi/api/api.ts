import {AnalysisControllerService} from './analysisController.service';

export * from './dataModelingController.service';
import { DataModelingControllerService } from './dataModelingController.service';
import {DataSourceControllerService} from './dataSourceController.service';
export const APIS = [DataModelingControllerService, AnalysisControllerService, DataSourceControllerService];
