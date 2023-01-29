import { Component, Input, OnInit } from '@angular/core';
import { ModelingInfo } from '../../../../projects/modeling/src/lib/api/generated/dms';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { NzModalRef } from 'ng-zorro-antd/modal';

@Component({
  selector: 'app-edit-modeling',
  templateUrl: './edit-modeling.component.html',
  styleUrls: ['./edit-modeling.component.less']
})
export class EditModelingComponent implements OnInit {


  @Input() modeling: ModelingInfo;

  form: FormGroup;

  constructor(private modal: NzModalRef) {
  }

  ngOnInit(): void {
    console.log(this.modeling);
    this.form = new FormGroup({
      title: new FormControl(this.modeling.name, [Validators.required, Validators.minLength(2), Validators.maxLength(255)]),
      description: new FormControl(this.modeling.description, [Validators.required, Validators.maxLength(3500)])
    });

  }

  submit(): void {
    this.modal.destroy({
      name: this.form.get('title').value,
      description: this.form.get('description').value
    });
  }

  cancel(): void {
    this.modal.destroy();
  }
}
