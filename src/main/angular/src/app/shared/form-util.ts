import {FormControl, FormGroup, UntypedFormArray, UntypedFormGroup, Validators} from '@angular/forms';


export function getValidString(input: any): string | undefined {
  if (!input || input.length === 0) {
    return undefined;
  }
  return input.toString();
}

export function getValidInt(input: any): number | undefined {
  if (!input) {
    return undefined;
  }
  return input.toString().length > 0 ? Number.parseInt(input, 10) : undefined;
}

export function getValidFloat(input: any): number | undefined {
  if (!input) {
    return undefined;
  }
  return input.toString().length > 0 ? Number.parseFloat(input) : undefined;
}

export function isRequired(form: FormGroup, formControlName: string) {
  const control = form.controls[formControlName] as FormControl;
  if(control) {
    return control.hasValidator(Validators.required);
  }
  return false;
}

export function buildFormArrayWithEmptyFormGroups(items: Array<any>): UntypedFormArray {
  const formArray = new UntypedFormArray([]);
  if (items) {
    items.forEach(() => formArray.push(new UntypedFormGroup({})));
  }
  return formArray;
}
