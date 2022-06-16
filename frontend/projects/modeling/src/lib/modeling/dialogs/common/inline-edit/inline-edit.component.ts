import { Component, ElementRef, forwardRef, Input, OnInit, Renderer2 } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { BehaviorSubject } from 'rxjs';
import { EditState } from '../../semantic-node-dialog/model/edit-state';

@Component({
  selector: 'pls-inline-edit',
  templateUrl: './inline-edit.component.html',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => InlineInputEditComponent),
      multi: true
    }
  ]
})
export class InlineInputEditComponent implements ControlValueAccessor, OnInit {

  // The control label
  @Input() label = '';
  // Type of input control
  @Input() type = 'text';
  // Input value required
  @Input() required = false;
  // Input control is disabled
  @Input() disabled = false;
  // color of the confirm button
  @Input() confirmColor = 'primary';
  // color of the cancel button
  @Input() cancelColor = 'warn';

  @Input() $edit: BehaviorSubject<EditState>;

  @Input() empty = '';

  get editing(): boolean {
    return this._editing;
  }

  private _editing = false;
  // private value of input
  private _value = '';
  // value prior to editing
  private preValue = '';
  // Callback when the value is changing
  public onChange: any = Function.prototype;
  // Callback when the input is accessed
  public onTouched: any = Function.prototype;

  ngOnInit(): void {
    this.$edit.subscribe(state => {
      switch (state) {
        case EditState.start:
          this.start();
          break;
        case EditState.confirm:
          this.confirm();
          break;
        case EditState.cancel:
          this.cancel();
          break;
      }
    });
  }

  get value(): any {
    return this._value;
  }

  set value(v: any) {
    if (v !== this._value) {
      this._value = v;
      this.onChange(v);
    }
  }

  // ControlValueAccessor interface impl
  writeValue(value: any): void {
    if (value !== undefined) {
      this._value = value;
    }
  }

  // ControlValueAccessor interface impl
  public registerOnChange(fn: (_: any) => {}): void {
    this.onChange = fn;
  }

  // ControlValueAccessor interface impl
  public registerOnTouched(fn: () => {}): void {
    this.onTouched = fn;
  }

  cancel(): void {
    this.value = this.preValue;
    this._editing = false;
  }

  confirm(): void {
    this._editing = false;
  }

  keypress($event): void {
  }

  // Start editing
  start(): void {
    if (this.disabled) {
      return;
    }
    this._editing = true;
    console.log('old value: ' + this.value);
    this.preValue = this.value;
  }

  constructor(element: ElementRef, private _renderer: Renderer2) {
  }


}
