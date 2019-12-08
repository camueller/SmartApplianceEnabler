import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {FormHandler} from '../shared/form-handler';
import {Logger} from '../log/logger';
import {HttpConfiguration} from './http-configuration';
import {getValidString} from '../shared/form-util';

@Component({
  selector: 'app-http-configuration',
  templateUrl: './http-configuration.component.html',
  styleUrls: ['../global.css']
})
export class HttpConfigurationComponent implements OnChanges, OnInit {
  @Input()
  httpConfiguration: HttpConfiguration;
  @Input()
  form: FormGroup;
  formHandler: FormHandler;

  constructor(private logger: Logger,
  ) {
    this.formHandler = new FormHandler();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.httpConfiguration) {
      if (changes.httpConfiguration.currentValue) {
        this.httpConfiguration = changes.httpConfiguration.currentValue;
      } else {
        this.httpConfiguration = new HttpConfiguration();
      }
      this.updateForm();
    }
    if (changes.form) {
      this.expandParentForm();
    }
  }

  ngOnInit(): void {
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
