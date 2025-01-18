import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnChanges,
  OnInit,
  QueryList,
  SimpleChanges,
  ViewChild,
  ViewChildren
} from '@angular/core';
import {ControlDefaults} from '../control-defaults';
import {ControlContainer, FormControl, FormGroup, FormGroupDirective} from '@angular/forms';
import {ErrorMessages} from '../../shared/error-messages';
import {ErrorMessageHandler} from '../../shared/error-message-handler';
import {Logger} from '../../log/logger';
import {TranslateService} from '@ngx-translate/core';
import {HttpSwitch} from './http-switch';
import {ErrorMessage, ValidatorType} from '../../shared/error-message';
import {ControlValueName} from '../control-value-name';
import {HttpReadComponent} from '../../http/read/http-read.component';
import {HttpConfigurationComponent} from '../../http/configuration/http-configuration.component';
import {HttpWriteComponent} from '../../http/write/http-write.component';
import {HttpWrite} from '../../http/write/http-write';
import {isControlValid} from '../control-validator';
import {HttpRead} from '../../http/read/http-read';
import {ControlHttpModel} from './control-http.model';
import {buildFormArrayWithEmptyFormGroups} from '../../shared/form-util';
import {HttpReadModel} from '../../http/read/http-read.model';
import {HttpWriteModel} from '../../http/write/http-write.model';

@Component({
    selector: 'app-control-http',
    templateUrl: './control-http.component.html',
    styleUrls: ['./control-http.component.scss'],
    viewProviders: [
        { provide: ControlContainer, useExisting: FormGroupDirective }
    ],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class ControlHttpComponent implements OnChanges, OnInit {
  @Input()
  httpSwitch: HttpSwitch;
  @ViewChild('httpConfigurationComponent', {static: true})
  httpConfigurationComp: HttpConfigurationComponent;
  @ViewChildren('httpWriteComponents')
  httpWriteComps: QueryList<HttpWriteComponent>;
  @ViewChild('httpReadComponent')
  httpReadComp: HttpReadComponent;
  @Input()
  controlDefaults: ControlDefaults;
  @Input()
  form: FormGroup<ControlHttpModel>;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private translate: TranslateService,
              private changeDetectorRef: ChangeDetectorRef
  ) {
    this.errorMessageHandler = new ErrorMessageHandler(logger);
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.form = this.parent.form;
    if (changes.httpSwitch) {
      if (changes.httpSwitch.currentValue) {
        this.httpSwitch = changes.httpSwitch.currentValue;
      } else {
        this.httpSwitch = new HttpSwitch();
        this.httpSwitch.httpWrites = [HttpWrite.createWithSingleChild()];
      }
      this.expandParentForm();
    }
  }

  ngOnInit() {
    this.errorMessages = new ErrorMessages('ControlHttpComponent.error.', [
      new ErrorMessage('onUrl', ValidatorType.required),
      new ErrorMessage('onUrl', ValidatorType.pattern),
      new ErrorMessage('offUrl', ValidatorType.required),
      new ErrorMessage('offUrl', ValidatorType.pattern),
    ], this.translate);
    this.expandParentForm();
    this.form.statusChanges.subscribe(() => {
      this.errors = this.errorMessageHandler.applyErrorMessages(this.form, this.errorMessages);
    });
  }

  get valueNames() {
    return [ControlValueName.On, ControlValueName.Off];
  }

  get readValueNames() {
    return [ControlValueName.On];
  }

  get valueNameTextKeys() {
    return ['ControlHttpComponent.On', 'ControlHttpComponent.Off'];
  }

  get readValueNameTextKeys() {
    return ['ControlHttpComponent.read.On'];
  }

  get isAddHttpWritePossible() {
    if (this.httpSwitch.httpWrites.length === 1) {
      return this.httpSwitch.httpWrites[0].writeValues.length < 2;
    }
    return this.httpSwitch.httpWrites.length < 2;
  }

  get maxValues() {
    return this.httpSwitch.httpWrites.length === 2 ? 1 : 2;
  }

  get readControlState() {
    return !!this.form.controls.httpRead;
  }

  set readControlState(readControlState: boolean) {
    if (readControlState) {
      if(!this.form.controls.httpRead) {
        this.form.addControl('httpRead', new FormGroup({} as HttpReadModel));
      }
    } else {
      if(this.form.controls.httpRead) {
        this.form.removeControl('httpRead');
      }
    }
  }

  addHttpWrite() {
    this.httpSwitch.httpWrites.push(HttpWrite.createWithSingleChild());
    this.httpWritesFormArray.push(new FormGroup({} as HttpWriteModel));
    this.form.markAsDirty();
    this.changeDetectorRef.detectChanges();
  }

  onHttpWriteRemove(index: number) {
    this.httpSwitch.httpWrites.splice(index, 1);
    this.httpWritesFormArray.removeAt(index);
    this.form.markAsDirty();
  }

  get httpWritesFormArray() {
    return this.form.controls.httpWrites;
  }

  getHttpWriteFormGroup(index: number) {
    return this.httpWritesFormArray.controls[index];
  }

  getHttpReadFormGroup() {
    return this.form.controls.httpRead;
  }

  expandParentForm() {
    this.form.addControl('httpWrites', buildFormArrayWithEmptyFormGroups(this.httpSwitch.httpWrites));
    const readControlState = this.httpSwitch.httpRead.readValues.length > 0;
    this.form.addControl('readControlState', new FormControl(readControlState));
    this.readControlState = readControlState;
    this.form.controls.readControlState.valueChanges.subscribe(value => {
      if (value && !this.httpSwitch.httpRead) {
        this.httpSwitch.httpRead = HttpRead.createWithSingleChild();
      }
      this.readControlState = value;
      this.form.markAsDirty();
    });
    this.form.setValidators(isControlValid(this.form, 'httpWrites', 'httpWriteValues'));
  }

  updateModelFromForm(): HttpSwitch | undefined {
    const httpConfiguration = this.httpConfigurationComp.updateModelFromForm();
    const httpWrites = [];
    this.httpWriteComps.forEach(httpWriteComponent => {
      const httpWrite = httpWriteComponent.updateModelFromForm();
      if (httpWrite) {
        httpWrites.push(httpWrite);
      }
    });
    const httpRead = this.httpReadComp && this.httpReadComp.updateModelFromForm();

    if (!(httpConfiguration || httpWrites.length > 0 || httpRead)) {
      return undefined;
    }

    this.httpSwitch.httpConfiguration = httpConfiguration;
    this.httpSwitch.httpWrites = httpWrites;
    this.httpSwitch.httpRead = httpRead;
    return this.httpSwitch;
  }
}
