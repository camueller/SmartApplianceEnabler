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
  formHandler: FormHandler;
  form: FormGroup;

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
  ) {
    this.formHandler = new FormHandler();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.form = this.parent.form;
    if (changes.httpConfiguration) {
      if (changes.httpConfiguration.currentValue) {
        this.httpConfiguration = changes.httpConfiguration.currentValue;
      } else {
        this.httpConfiguration = new HttpConfiguration();
      }
      this.updateForm();
    }
  }

  ngOnInit(): void {
    this.expandParentForm();
  }

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'contentType',
      this.httpConfiguration && this.httpConfiguration.contentType);
    this.formHandler.addFormControl(this.form, 'username',
      this.httpConfiguration && this.httpConfiguration.username);
    this.formHandler.addFormControl(this.form, 'password',
      this.httpConfiguration && this.httpConfiguration.password);
  }

  updateForm() {
    this.formHandler.setFormControlValue(this.form, 'contentType', this.httpConfiguration.contentType);
    this.formHandler.setFormControlValue(this.form, 'username', this.httpConfiguration.username);
    this.formHandler.setFormControlValue(this.form, 'password', this.httpConfiguration.password);
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
