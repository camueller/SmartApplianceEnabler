import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {ControlContainer, FormGroup, FormGroupDirective} from '@angular/forms';
import {FormHandler} from '../shared/form-handler';
import {Logger} from '../log/logger';
import {NestedFormService} from '../shared/nested-form-service';
import {FormMarkerService} from '../shared/form-marker-service';
import {TranslateService} from '@ngx-translate/core';
import {HttpConfiguration} from './http-configuration';
import {getValidString} from '../shared/form-util';
import {Subscription} from 'rxjs';

@Component({
  selector: 'app-http-configuration',
  templateUrl: './http-configuration.component.html',
  styleUrls: ['../global.css'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class HttpConfigurationComponent implements OnInit, OnDestroy {
  @Input()
  httpConfiguration: HttpConfiguration;
  form: FormGroup;
  formHandler: FormHandler;
  nestedFormServiceSubscription: Subscription;

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
    this.nestedFormServiceSubscription = this.nestedFormService.submitted.subscribe(
      () => this.updateHttpConfiguration(this.httpConfiguration, this.form));
  }

  ngOnDestroy() {
    this.nestedFormServiceSubscription.unsubscribe();
  }

  expandParentForm(form: FormGroup, httpConfiguration: HttpConfiguration, formHandler: FormHandler) {
    formHandler.addFormControl(form, 'contentType',
      httpConfiguration ? httpConfiguration.contentType : undefined);
    formHandler.addFormControl(form, 'username',
      httpConfiguration ? httpConfiguration.username : undefined);
    formHandler.addFormControl(form, 'password',
      httpConfiguration ? httpConfiguration.password : undefined);
  }

  updateHttpConfiguration(httpConfiguration: HttpConfiguration, form: FormGroup) {
    httpConfiguration.contentType = getValidString(form.controls.contentType.value);
    httpConfiguration.username = getValidString(form.controls.username.value);
    httpConfiguration.password = getValidString(form.controls.password.value);
    this.nestedFormService.complete();
  }
}
