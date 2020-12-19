import { FormElementType } from './FormElementType';

export interface FormElement<T> {
  type: FormElementType | string;
  label: string;
  minimumCardinality: number;
  maximumCardinality: number;
  value: T[];
  hidden: boolean;
}
