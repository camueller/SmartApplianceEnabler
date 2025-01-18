import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {FormControl, FormGroup} from '@angular/forms';
import { isRequired } from 'src/app/shared/form-util';
import {Logger} from '../../log/logger';
import {HttpConfiguration} from './http-configuration';
import {HttpConfigurationModel} from './http-configuration.model';

@Component({
    selector: 'app-http-configuration',
    templateUrl: './http-configuration.component.html',
    styleUrls: ['./http-configuration.component.scss'],
    standalone: false
})
export class HttpConfigurationComponent implements OnChanges, OnInit {
  @Input()
  httpConfiguration: HttpConfiguration;
  @Input()
  form: FormGroup<HttpConfigurationModel>;

  constructor(private logger: Logger,
  ) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.httpConfiguration) {
      if (changes.httpConfiguration.currentValue) {
        this.httpConfiguration = changes.httpConfiguration.currentValue;
      } else {
        this.httpConfiguration = new HttpConfiguration();
      }
      if(! changes.httpConfiguration.isFirstChange()) {
        this.updateForm();
      }
    }
    if (changes.form) {
      this.expandParentForm();
    }
  }

  ngOnInit(): void {
  }

  isRequired(formControlName: string) {
    return isRequired(this.form, formControlName);
  }

  expandParentForm() {
    this.form.addControl('contentType', new FormControl(this.httpConfiguration?.contentType))
    this.form.addControl('username', new FormControl(this.httpConfiguration?.username))
    this.form.addControl('password', new FormControl(this.httpConfiguration?.password))
  }

  updateForm() {
    this.form.controls.contentType.setValue(this.httpConfiguration.contentType);
    this.form.controls.username.setValue(this.httpConfiguration.username);
    this.form.controls.password.setValue(this.httpConfiguration.password);
  }

  updateModelFromForm(): HttpConfiguration | undefined {
    const contentType = this.form.controls.contentType.value;
    const username = this.form.controls.username.value;
    const password = this.form.controls.password.value;

    if (!(contentType || username || password)) {
      return undefined;
    }

    this.httpConfiguration.contentType = contentType;
    this.httpConfiguration.username = username;
    this.httpConfiguration.password = password;
    return this.httpConfiguration;
  }
}
