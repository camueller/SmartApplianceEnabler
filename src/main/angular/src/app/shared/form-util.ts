import {
  AbstractControlOptions,
  AsyncValidatorFn,
  FormControl,
  FormGroup,
  ValidatorFn,
  Validators
} from '@angular/forms';

declare const $: any;

export class FormUtil {

  public static addFormControl(formGroup: FormGroup, name: string, formState?: any,
                 validatorOrOpts?: ValidatorFn | ValidatorFn[] | AbstractControlOptions | null,
                 asyncValidator?: AsyncValidatorFn | AsyncValidatorFn[] | null) {
    if (Array.isArray(validatorOrOpts)) {
      (validatorOrOpts as []).forEach((validator: ValidatorFn) => {
        if (validator === Validators.required) {
          $(`input[formControlName=${name}]`).parent().children('label').addClass('required');
        }
      });
    }
    const control = new FormControl(formState, validatorOrOpts, asyncValidator);
    formGroup.addControl(name, control);
  }
}
