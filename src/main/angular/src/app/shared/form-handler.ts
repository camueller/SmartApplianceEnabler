import {
  AbstractControlOptions,
  AsyncValidatorFn, FormArray,
  FormControl,
  FormGroup,
  ValidatorFn,
  Validators
} from '@angular/forms';

export class FormHandler {

  formControlNamesRequired: string[] = [];

  public addFormControl(formGroup: FormGroup, formControlName: string, formState?: any,
                        validatorOrOpts?: ValidatorFn | ValidatorFn[] | AbstractControlOptions | null,
                        asyncValidator?: AsyncValidatorFn | AsyncValidatorFn[] | null) {
    this.registerValidators(formControlName, validatorOrOpts);
    const control = new FormControl(formState, validatorOrOpts, asyncValidator);
    formGroup.addControl(formControlName, control);
  }

  public setFormControlValue(form: FormGroup, formControlName: string, value: any) {
    if (form) {
      const control = form.controls[formControlName];
      if (control) {
        control.setValue(value);
      }
    }
  }

  public addFormArrayControlWithEmptyFormGroups(form: FormGroup, formControlName: string, items: Array<any>) {
    form.addControl(formControlName, this.buildFormArrayWithEmptyFormGroups(items));
  }

  public setFormArrayControlWithEmptyFormGroups(form: FormGroup, formControlName: string, items: Array<any>) {
    form.setControl(formControlName, this.buildFormArrayWithEmptyFormGroups(items));
  }

  public addFormControlToFormArray(formArray: FormArray, formArrayIndex: number, formControlName: string, validatorOrOpts?: ValidatorFn | ValidatorFn[] | AbstractControlOptions | null) {
    (formArray.at(formArrayIndex) as FormGroup).addControl(formControlName, new FormControl(undefined, validatorOrOpts));
    this.registerValidators(formControlName, validatorOrOpts);
  }

  private buildFormArrayWithEmptyFormGroups(items: Array<any>): FormArray {
    const formArray = new FormArray([]);
    if (items) {
      items.forEach(() => formArray.push(new FormGroup({})));
    }
    return formArray;
  }

  registerValidators(formControlName: string, validatorOrOpts?: ValidatorFn | ValidatorFn[] | AbstractControlOptions | null) {
    if (Array.isArray(validatorOrOpts)) {
      (validatorOrOpts as []).forEach((validatorFn: ValidatorFn) => {
        this.registerRequiredValidator(validatorFn, formControlName);
      });
    } else {
      this.registerRequiredValidator(validatorOrOpts, formControlName);
    }
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
