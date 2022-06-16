import { Component, EventEmitter, Output} from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { CombinedModel, ModelingControllerService, ModelingInfo, SyntaxModel } from '@tmdt/dms';
import { v4 as uuidv4 } from 'uuid';
import { NzNotificationService } from 'ng-zorro-antd/notification';
import { Router} from '@angular/router';
import { appRoutesNames } from '../../app.routes.names';

@Component({
  selector: 'app-create-modeling',
  templateUrl: './create-modeling.component.html',
  styleUrls: ['./create-modeling.component.less'],
})
export class CreateModelingComponent {

  dataId: string;
  modelId: string;
  syntaxModel: SyntaxModel;

  // form
  form = new FormGroup({
    title: new FormControl('', [Validators.required, Validators.minLength(2), Validators.maxLength(255)]),
    description: new FormControl('', [Validators.required, Validators.maxLength(4000)])
  });
  error;
  success;

  @Output() modelingCreated = new EventEmitter();


  constructor(private modelingController: ModelingControllerService, private notification: NzNotificationService, private router: Router) {
    this.dataId = uuidv4();
    this.modelId = this.dataId;
  }


  onSubmit(): void {
    this.error = null;
    const combinedModel: CombinedModel = {id: this.modelId, syntaxModel: this.syntaxModel};
    this.modelingController.createNewModeling(combinedModel, null, false,
      this.form.get('title').value, this.form.get('description').value, this.dataId)
      .subscribe((res: ModelingInfo) => {
        if ('id' in res) {
          this.form.reset();
          this.router.navigate([`${appRoutesNames.MODELS}/${res.id}`]);
        }
      }, error => {
        console.log('create error', error);
        this.error = error;
      });
  }

  /**
   * Returns true of from is valid and a schema is available
   */
  isValid(): boolean {
    return this.form.valid && !!this.syntaxModel;
  }

  onSchemaAvailable(schema: SyntaxModel): void {
    this.syntaxModel = schema;
  }

  onSchemaAnalysisError(errorMessage: string): void {
    this.syntaxModel = undefined;
  }
}
