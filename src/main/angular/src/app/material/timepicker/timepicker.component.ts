import {AfterViewChecked, Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {FormControl, FormControlName, FormGroup, FormGroupDirective, Validators} from '@angular/forms';
import {ErrorMessages} from '../../shared/error-messages';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {Logger} from '../../log/logger';
import {InputValidatorPatterns} from '../../shared/input-validator-patterns';

declare const $: any;

/**
 * The time set by clock picker is displayed in input field but not set in the form model.
 * Since there is no direct access to the native element from the form control we have to add a hook to
 * propagate time changes on the native element to the form control.
 * Inspired by https://stackoverflow.com/questions/39642547/is-it-possible-to-get-native-element-for-formcontrol
 */
const originFormControlNameNgOnChanges = FormControlName.prototype.ngOnChanges;
FormControlName.prototype.ngOnChanges = function () {
  const result = originFormControlNameNgOnChanges.apply(this, arguments);
  this.control.nativeElement = this.valueAccessor._elementRef;

  const elementRef = this.valueAccessor._elementRef;
  if (elementRef) {
    const classAttribute: string = elementRef.nativeElement.attributes.getNamedItem('class');
    if (classAttribute != null) {
      const classAttributeValues = classAttribute['nodeValue'];
      if (classAttributeValues.indexOf('clockpicker') > -1) {
        $(this.valueAccessor._elementRef.nativeElement).on('change', (event) => {
          this.control.setValue(event.target.value);
          this.control.markAsDirty();
        });
      }
    }
  }
  return result;
};

@Component({
  selector: 'app-timepicker',
  templateUrl: './timepicker.component.html',
  styles: []
})
export class TimepickerComponent implements OnChanges, OnInit, AfterViewChecked {
  @Input()
  formControlNameTP: string;
  @Input()
  value: string;
  @Input()
  label: string;
  @Input()
  tooltip: string;
  @Input()
  required: boolean;
  @Input()
  enabled = true;
  @Input()
  noErrorOnField: boolean;
  @Input()
  width: string;
  form: FormGroup;
  initializeOnceAfterViewChecked = false;
  errors: { [key: string]: string } = {};
  @Input()
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private parent: FormGroupDirective) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.form = this.parent.form;
    if (changes.value) {
      this.updateForm();
    }
    if (changes.enabled) {
      this.setEnabled(changes.enabled.currentValue);
    }
  }

  ngOnInit(): void {
    this.form.addControl(this.formControlNameTP, new FormControl(undefined, [Validators.pattern(InputValidatorPatterns.TIME_OF_DAY_24H)]));
    this.updateForm();
    if (this.errorMessages) {
      this.form.statusChanges.subscribe(() => {
        this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
      });
    }
    this.initializeOnceAfterViewChecked = true;
  }

  ngAfterViewChecked() {
    if (this.initializeOnceAfterViewChecked) {
      this.initializeOnceAfterViewChecked = false;
      this.initializeClockPicker();
    }
  }

  initializeClockPicker() {
    $('.clockpicker').clockpicker({autoclose: true});
  }

  updateForm() {
    const control = this.form.controls[this.formControlNameTP];
    if (control) {
      control.setValue(this.value);
    }
  }

  setEnabled(enabled: boolean) {
    const control = this.form.controls[this.formControlNameTP];
    if (control) {
      if (enabled) {
        control.enable();
      } else {
        control.disable();
      }
    }
  }

  get error() {
    return this.errors[this.formControlNameTP];
  }

  get cssClasses() {
    return this.noErrorOnField ? 'no-error-on-field' : '';
  }

  updateModelFromForm(): string | undefined {
    const control = this.form.controls[this.formControlNameTP];
    if (control) {
      return control.value;
    }
  }
}
