import { Component, Input } from '@angular/core';
import { NzModalRef } from 'ng-zorro-antd/modal';
import { SyntacticOperationDTO } from '../../../../api/generated/dms';


@Component({
  selector: 'pls-operations-dialog',
  templateUrl: './operations-dialog.component.html',
  styleUrls: ['../../common/dialog.styles.scss']
})
export class PlsOperationsDialogComponent {

  @Input() operation: SyntacticOperationDTO;

  error;

  constructor(private modalRef: NzModalRef<PlsOperationsDialogComponent>) {
  }

  apply(): void {
    this.error = null;
    this.modalRef.close(this.operation);
  }

  cancel(): void {
    this.error = null;
    this.modalRef.close();
  }
}
