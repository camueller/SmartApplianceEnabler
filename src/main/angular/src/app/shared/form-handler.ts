import {
  AbstractControlOptions,
  AsyncValidatorFn,
  FormControl,
  FormGroup,
  ValidatorFn,
  Validators
} from '@angular/forms';

declare const $: any;

export class FormHandler {

  formControlNamesRequired: string[] = [];

  public addFormControl(formGroup: FormGroup, name: string, formState?: any,
                               validatorOrOpts?: ValidatorFn | ValidatorFn[] | AbstractControlOptions | null,
                               asyncValidator?: AsyncValidatorFn | AsyncValidatorFn[] | null) {
    if (Array.isArray(validatorOrOpts)) {
      (validatorOrOpts as []).forEach((validator: ValidatorFn) => {
        if (validator === Validators.required) {
          this.formControlNamesRequired.push(name);
        }
      });
    }
    const control = new FormControl(formState, validatorOrOpts, asyncValidator);
    formGroup.addControl(name, control);
  }

  public markLabelsRequired() {
    this.formControlNamesRequired.forEach(formControlName => {
      $(`input[formControlName=${formControlName}]`).parent().children('label').addClass('required');
      $(`select[formControlName=${formControlName}]`).parent().children('label').addClass('required');
      $(`sui-select[formControlName=${formControlName}]`).parent().children('label').addClass('required');
    });
  }
}
