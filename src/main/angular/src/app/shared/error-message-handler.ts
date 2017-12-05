import {FormArray, FormGroup, NgForm} from '@angular/forms';
import {ErrorMessages} from './error-messages';
import {ValidatorType} from './error-message';

export class ErrorMessageHandler {

  public static applyErrorMessages4TemplateDrivenForm(formViewChild: NgForm,
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
    console.log('ERRORS=' + JSON.stringify(errors));
    return errors;
  }

  public static applyErrorMessages4ReactiveForm(form: FormGroup, errorMessages: ErrorMessages, indexed: boolean,
                                                controlPrefix = ''): { [key: string]: string } {
    const errors: { [key: string]: string } = {};
    if ((<FormArray> form.controls.schedules).controls.length > 0) {
      for (const message of errorMessages.getErrorMessages()) {
        for (let i = 0; i < (<FormArray> form.controls.schedules).controls.length; i++) {
          const keyWithPrefix = controlPrefix + message.forControl;
          const keyWithWithPeriods = keyWithPrefix.replace('_', '.');
          const key = keyWithWithPeriods.replace('#', i.toString());
          console.log('KEY=' + key);
          const control = form.get(key);
          if (control && control.dirty && control.invalid
            && control.errors[ValidatorType[message.forValidator]] && !errors[message.forControl]) {
            if (indexed) {
              errors[message.forControl + '.' + i.toString()] = message.text;
            } else {
              errors[message.forControl] = message.text;
            }
          }
        }
      }
    }
    console.log('ERRORS=' + JSON.stringify(errors));
    return errors;
  }
}
