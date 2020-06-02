import {Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {StatusService} from '../status.service';
import {TimeUtil} from '../../shared/time-util';
import {FormHandler} from '../../shared/form-handler';
import {ErrorMessages} from '../../shared/error-messages';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {ErrorMessage, ValidatorType} from '../../shared/error-message';
import {TranslateService} from '@ngx-translate/core';
import {Logger} from '../../log/logger';
import {TimepickerComponent} from '../../material/timepicker/timepicker.component';

@Component({
  selector: 'app-status-edit',
  templateUrl: './status-edit.component.html',
  styleUrls: ['./status-edit.component.scss', '../status.component.scss']
})
export class StatusEditComponent implements OnInit {
  @Input()
  applianceId: string;
  @Output()
  beforeFormSubmit = new EventEmitter<any>();
  @Output()
  formSubmitted = new EventEmitter<any>();
  @Output()
  formCancelled = new EventEmitter<any>();
  @ViewChild('runtimeComponent', {static: true})
  runtimeComp: TimepickerComponent;
  form: FormGroup;
  formHandler: FormHandler;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;
  suggestedRuntime: string | undefined;

  constructor(private logger: Logger,
              private statusService: StatusService,
              private translate: TranslateService) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
    this.formHandler = new FormHandler();
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('StatusEditComponent.error.', [
      new ErrorMessage('runtime', ValidatorType.required),
      new ErrorMessage('runtime', ValidatorType.pattern),
    ], this.translate);
    this.buildForm();
    this.statusService.suggestRuntime(this.applianceId).subscribe(suggestedRuntime => {
      this.suggestedRuntime = TimeUtil.toHourMinute(Number.parseInt(suggestedRuntime, 10));
    });
  }

  buildForm() {
    this.form = new FormGroup({});
  }

  get hasErrors(): boolean {
    return Object.keys(this.errors).length > 0;
  }

  get error(): string {
    const errors = Object.values(this.errors);
    return errors.length > 0 ? errors[0] : undefined;
  }

  cancelForm() {
    this.formCancelled.emit();
  }

  submitForm() {
    this.beforeFormSubmit.emit();
    const runtime = this.runtimeComp.updateModelFromForm();
    const seconds = TimeUtil.toSeconds(runtime);
    this.statusService.setRuntime(this.applianceId, seconds).subscribe(() => {
      this.statusService.toggleAppliance(this.applianceId, true).subscribe(
        () => this.formSubmitted.emit());
    });
  }
}
