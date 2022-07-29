import {
  AbstractControlOptions,
  AsyncValidatorFn, UntypedFormArray,
  UntypedFormControl,
  UntypedFormGroup,
  ValidatorFn,
  Validators
} from '@angular/forms';

export class FormHandler {

  formControlNamesRequired: string[] = [];

  public addFormControl(formGroup: UntypedFormGroup, formControlName: string, formState?: any,
                        validatorOrOpts?: ValidatorFn | ValidatorFn[] | AbstractControlOptions | null,
                        asyncValidator?: AsyncValidatorFn | AsyncValidatorFn[] | null) {
    if (Array.isArray(validatorOrOpts)) {
      (validatorOrOpts as []).forEach((validatorFn: ValidatorFn) => {
        this.registerRequiredValidator(validatorFn, formControlName);
      });
    } else {
      this.registerRequiredValidator(validatorOrOpts, formControlName);
    }
    const control = new UntypedFormControl(formState, validatorOrOpts, asyncValidator);
    formGroup.addControl(formControlName, control);
  }

  public setFormControlValue(form: UntypedFormGroup, formControlName: string, value: any) {
    if (form) {
      const control = form.controls[formControlName];
      if (control) {
        control.setValue(value);
      }
    }
  }

  public addFormArrayControlWithEmptyFormGroups(form: UntypedFormGroup, formControlName: string, items: Array<any>) {
    form.addControl(formControlName, this.buildFormArrayWithEmptyFormGroups(items));
  }

  public setFormArrayControlWithEmptyFormGroups(form: UntypedFormGroup, formControlName: string, items: Array<any>) {
    form.setControl(formControlName, this.buildFormArrayWithEmptyFormGroups(items));
  }

  private buildFormArrayWithEmptyFormGroups(items: Array<any>): UntypedFormArray {
    const formArray = new UntypedFormArray([]);
    if (items) {
      items.forEach(() => formArray.push(new UntypedFormGroup({})));
    }
    return formArray;
  }

  registerRequiredValidator(validatorFn: any, formControlName: string) {
    if (validatorFn === Validators.required) {
      this.formControlNamesRequired.push(formControlName);
    }
  }

  public isRequired(formControlName: string) {
    return this.formControlNamesRequired.indexOf(formControlName) >= 0;
  }
}
