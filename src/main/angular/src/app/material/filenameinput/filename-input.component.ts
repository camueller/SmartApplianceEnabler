import {Component, Input, OnInit} from '@angular/core';
import {Logger} from '../../log/logger';
import {AbstractControl, FormGroup, FormGroupDirective} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {FormHandler} from '../../shared/form-handler';
import {SettingsService} from '../../settings/settings-service';
import {FileMode} from './file-mode';
import {debounce} from 'rxjs/operators';
import {interval} from 'rxjs';
import {getValidString} from '../../shared/form-util';

@Component({
  selector: 'app-filenameinput',
  templateUrl: './filename-input.component.html',
  styleUrls: ['./filename-input.component.scss']
})
export class FilenameInputComponent implements OnInit {
  @Input()
  formControlNameInput: string;
  @Input()
  required: boolean;
  @Input()
  label: string;
  @Input()
  value: string;
  @Input()
  requiredFileModes: FileMode[];
  form: FormGroup;
  formHandler: FormHandler;
  errors: { [key: string]: string } = {};

  constructor(private logger: Logger,
              private settingsService: SettingsService,
              private translate: TranslateService,
              private parent: FormGroupDirective) {
    this.formHandler = new FormHandler();
  }

  ngOnInit(): void {
    this.form = this.parent.form;
    this.translate.get([
      'FilenameInputComponent.error.missing',
      'FilenameInputComponent.error.not-readable',
      'FilenameInputComponent.error.not-writable',
      'FilenameInputComponent.error.not-executable',
    ]).subscribe(translated => this.errors = translated);
    this.formHandler.addFormControl(this.form, this.formControlNameInput, this.value);
    const control = this.getControl();
    control.valueChanges.pipe(debounce(() => interval(400))).subscribe(() => this.checkFileAndSetError(control));
    this.checkFileAndSetError(control);
  }

  private getControl(): AbstractControl {
    return this.form.controls[this.formControlNameInput];
  }

  public get valid() {
    return this.errorKeys.length === 0;
  }

  public get isDisplayValidationIcon() {
    const value = this.getControl().value;
    return value && value.toString().length > 0;
  }

  public get validationIcon() {
    return this.valid ? 'check' : 'close';
  }

  public get validationIconClass() {
    return this.valid ? 'FilenameInputComponent__icon--valid' : 'FilenameInputComponent__icon--invalid';
  }

  public get errorKeys() {
    const errorKeys = this.form.controls[this.formControlNameInput].errors;
    return errorKeys ? Object.keys(errorKeys) : [];
  }

  public get errorMessage() {
    return this.errorKeys.length > 0 ? this.errors[this.errorKeys[0]] : undefined;
  }

  checkFileAndSetError(control: AbstractControl) {
    if (control.value) {
      this.settingsService.getFileAttributes(control.value.toString()).subscribe((filemode) => {
        /* tslint:disable:no-bitwise */
        let error: string | undefined;
        if (filemode === -1) {
          error = 'FilenameInputComponent.error.missing';
        }
        if (this.requiredFileModes.includes(FileMode.read) && !(filemode & 1)) {
          error = 'FilenameInputComponent.error.not-readable';
        }
        if (this.requiredFileModes.includes(FileMode.write) && !(filemode & 2)) {
          error = 'FilenameInputComponent.error.not-writable';
        }
        if (this.requiredFileModes.includes(FileMode.execute) && !(filemode & 4)) {
          error = 'FilenameInputComponent.error.not-executable';
        }
        if (error) {
          control.setErrors({[error]: true});
          control.markAsTouched();
        }
        /* tslint:enable:no-bitwise */
      });
    }
  }

  updateModelFromForm(): string | undefined {
    return getValidString(this.getControl()?.value);
  }
}
