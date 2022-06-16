import { ModalOptions } from 'ng-zorro-antd/modal';

export const ModalBaseConfig: ModalOptions = {
  nzFooter: null,
  nzClosable: true,
};

export const ModalMouseEnabledConfig: ModalOptions = {
  nzFooter: null,
  nzMaskStyle: {margin: '0', display: 'none'},
  nzWrapClassName: 'no-pointer-events',
  nzClosable: true,
  nzMask: false,
  nzMaskClosable: true,
};

