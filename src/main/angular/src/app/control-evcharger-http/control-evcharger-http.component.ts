import {AfterViewChecked, Component, Input, OnDestroy, OnInit} from '@angular/core';
import {Settings} from '../settings/settings';
import {SettingsDefaults} from '../settings/settings-defaults';
import {FormGroupDirective} from '@angular/forms';
import {Logger} from '../log/logger';
import {NestedFormService} from '../shared/nested-form-service';
import {FormMarkerService} from '../shared/form-marker-service';
import {TranslateService} from '@ngx-translate/core';
import {EvHttpControl} from './ev-http-control';

@Component({
  selector: 'app-control-evcharger-http',
  templateUrl: './control-evcharger-http.component.html',
  styles: []
})
export class ControlEvchargerHttpComponent implements  OnInit, AfterViewChecked, OnDestroy {

  @Input()
  evHttpControl: EvHttpControl;
  @Input()
  settings: Settings;
  @Input()
  settingsDefaults: SettingsDefaults;
  // httpConfigurations: FormArray;
  // form: FormGroup;
  // formHandler: FormHandler;
  // @Input()
  // translationKeys: string[];
  // translatedStrings: string[];
  // errors: { [key: string]: string } = {};
  // errorMessages: ErrorMessages;
  // errorMessageHandler: ErrorMessageHandler;

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private nestedFormService: NestedFormService,
              private formMarkerService: FormMarkerService,
              private translate: TranslateService) {
    // this.errorMessageHandler = new ErrorMessageHandler(logger);
    // this.formHandler = new FormHandler();
  }

  ngOnInit() {
    console.log('evHttpControl=', this.evHttpControl);
    // this.errorMessages = new MeterHttpErrorMessages(this.translate);
    // this.form = this.parent.form;
    // this.expandParentForm(this.form, this.evModbusControl, this.formHandler);
    // this.form.statusChanges.subscribe(() => {
    //   this.errors = this.errorMessageHandler.applyErrorMessages4ReactiveForm(this.form, this.errorMessages);
    // });
    // this.translate.get(this.translationKeys).subscribe(translatedStrings => {
    //   this.translatedStrings = translatedStrings;
    // });
    // this.nestedFormService.submitted.subscribe(
    //   () => this.updateFromForm(this.evModbusControl, this.form));
    // this.formMarkerService.dirty.subscribe(() => this.form.markAsDirty());
  }

  ngAfterViewChecked() {
    // this.formHandler.markLabelsRequired();
  }

  ngOnDestroy() {
    // FIXME: erzeugt Fehler bei Wechsel des Zählertypes
    // this.nestedFormService.submitted.unsubscribe();
  }

  getFormControlPrefix(index: number) {
    return `read${index}.`;
  }

  get valueNames() {
    return ['Power'];
  }

  get valueNameTextKeys() {
    return ['MeterHttpComponent.power'];
  }
}
