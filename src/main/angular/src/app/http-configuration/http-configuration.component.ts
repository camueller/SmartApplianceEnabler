import {Component, Input, OnInit} from '@angular/core';
import {ControlContainer, FormGroup, FormGroupDirective} from '@angular/forms';
import {FormHandler} from '../shared/form-handler';
import {Logger} from '../log/logger';
import {NestedFormService} from '../shared/nested-form-service';
import {FormMarkerService} from '../shared/form-marker-service';
import {TranslateService} from '@ngx-translate/core';
import {HttpConfiguration} from './http-configuration';

@Component({
  selector: 'app-http-configuration',
  templateUrl: './http-configuration.component.html',
  styleUrls: ['../global.css'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class HttpConfigurationComponent implements OnInit {
  @Input()
  httpConfiguration: HttpConfiguration;
  form: FormGroup;
  formHandler: FormHandler;

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private nestedFormService: NestedFormService,
              private formMarkerService: FormMarkerService,
              private translate: TranslateService
  ) {
    this.formHandler = new FormHandler();
  }

  ngOnInit(): void {
    this.form = this.parent.form;
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

  updateHttpRead(httpConfiguration: HttpConfiguration, form: FormGroup) {
    // httpRead.contentType = this.form.controls[this.getFormControlName('contentType')].value;
    // httpRead.username = this.form.controls[this.getFormControlName('username')].value;
    // httpRead.password = this.form.controls[this.getFormControlName('password')].value;
    this.nestedFormService.complete();
  }
}
