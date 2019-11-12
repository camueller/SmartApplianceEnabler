import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ControlContainer, FormGroup, FormGroupDirective} from '@angular/forms';
import {FormHandler} from '../shared/form-handler';
import {Logger} from '../log/logger';
import {HttpConfiguration} from './http-configuration';
import {getValidString} from '../shared/form-util';

@Component({
  selector: 'app-http-configuration',
  templateUrl: './http-configuration.component.html',
  styleUrls: ['../global.css'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class HttpConfigurationComponent implements OnChanges, OnInit {
  @Input()
  httpConfiguration: HttpConfiguration;
  form: FormGroup;
  formHandler: FormHandler;

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
  ) {
    this.formHandler = new FormHandler();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.form = this.parent.form;
    if (changes.httpConfiguration) {
      this.httpConfiguration = changes.httpConfiguration.currentValue;
      this.updateForm(this.form, this.httpConfiguration, this.formHandler);
    }
  }

  ngOnInit(): void {
    this.httpConfiguration = this.httpConfiguration || new HttpConfiguration();
    this.expandParentForm(this.form, this.httpConfiguration, this.formHandler);
  }

  expandParentForm(form: FormGroup, httpConfiguration: HttpConfiguration, formHandler: FormHandler) {
    formHandler.addFormControl(form, 'contentType',
      httpConfiguration ? httpConfiguration.contentType : undefined);
    formHandler.addFormControl(form, 'username',
      httpConfiguration ? httpConfiguration.username : undefined);
    formHandler.addFormControl(form, 'password',
      httpConfiguration ? httpConfiguration.password : undefined);
  }

  updateForm(form: FormGroup, httpConfiguration: HttpConfiguration, formHandler: FormHandler) {
    formHandler.setFormControlValue(form, 'contentType', httpConfiguration.contentType);
    formHandler.setFormControlValue(form, 'username', httpConfiguration.username);
    formHandler.setFormControlValue(form, 'password', httpConfiguration.password);
  }

  updateModelFromForm(): HttpConfiguration | undefined {
    const contentType = getValidString(this.form.controls.contentType.value);
    const username = getValidString(this.form.controls.username.value);
    const password = getValidString(this.form.controls.password.value);

    if (!(contentType || username || password)) {
      return undefined;
    }

    this.httpConfiguration.contentType = contentType;
    this.httpConfiguration.username = username;
    this.httpConfiguration.password = password;
    return this.httpConfiguration;
  }
}
