import {AbstractControl, FormArray, FormGroup, NgForm} from '@angular/forms';
import {ErrorMessages} from './error-messages';
import {ValidatorType} from './error-message';
import {Logger} from '../log/logger';

export class ErrorMessageHandler {

  constructor(private logger: Logger) {
  }

  public applyErrorMessages4TemplateDrivenForm(formViewChild: NgForm,
                                                      errorMessages: ErrorMessages,
                                                      controlSuffix = ''): { [key: string]: string } {
    const errors: { [key: string]: string } = {};
    for (const message of errorMessages.getErrorMessages()) {
      const control = formViewChild.form.get(message.forControl + controlSuffix);
      if (control && control.dirty && control.invalid
        && control.errors[ValidatorType[message.forValidator]] && !errors[message.forControl]) {
        errors[message.forControl] = message.text;
      }
    }
    const errorsString = JSON.stringify(errors);
    if (errorsString.length > 2) {
      this.logger.debug('ERRORS=' + errorsString);
    }
    return errors;
  }

  public applyErrorMessages4ReactiveForm(form: FormGroup, errorMessages: ErrorMessages): { [key: string]: string } {
    const errors: { [key: string]: string } = {};
    const formControlKeys = Object.keys(form.controls);
    for (const formControlKey of formControlKeys) {
      const control = form.controls[formControlKey];
      if (control instanceof FormArray) {
        const formArrayControl = control as FormArray;
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
    if (control && control.dirty && control.invalid
      && control.errors[validatorType] && !errors[errorKey]) {
      errors[errorKey] = errorMessage;
    }
  }
}
