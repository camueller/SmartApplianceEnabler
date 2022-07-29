import {AbstractControl, UntypedFormArray, UntypedFormGroup} from '@angular/forms';
import {ErrorMessages} from './error-messages';
import {ValidatorType} from './error-message';
import {Logger} from '../log/logger';

export class ErrorMessageHandler {

  constructor(private logger: Logger) {
  }

  public applyErrorMessages(form: UntypedFormGroup, errorMessages: ErrorMessages): { [key: string]: string } {
    const errors: { [key: string]: string } = {};
    const formControlKeys = Object.keys(form.controls);
    for (const formControlKey of formControlKeys) {
      const control = form.controls[formControlKey];
      if (control instanceof UntypedFormArray) {
        const formArrayControl = control as UntypedFormArray;
        if (formArrayControl.controls.length > 0) {
          for (const message of errorMessages.getErrorMessages()) {
            for (let i = 0; i < formArrayControl.controls.length; i++) {
              const key = (formControlKey + '.#.' + message.forControl)
                .replace(/_/g, '.')
                .replace('#', i.toString());
              const controlWithinArray = form.get(key);
              this.validateControl(
                controlWithinArray,
                ValidatorType[message.forValidator],
                message.forControl + '.' + i.toString(),
                message.text,
                errors);
            }
          }
        }
      } else {
        for (const message of errorMessages.getErrorMessages()) {
          // console.log(`forControl=${message.forControl} formControlKey=${formControlKey}`)
          if (message.forControl === formControlKey) {
            this.validateControl(
              control,
              ValidatorType[message.forValidator],
              message.forControl,
              message.text,
              errors);
          }
        }
      }
    }
    return errors;
  }

  private validateControl(control: AbstractControl, validatorType: string,
                          errorKey: string, errorMessage: string, errors: { [key: string]: string }) {
    // console.log(`errorKey=${errorKey}`)
    if (control && control.dirty && control.invalid
      && control.errors[validatorType] && !errors[errorKey]) {
      errors[errorKey] = errorMessage;
    }
  }
}
