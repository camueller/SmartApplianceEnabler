import {Component, Input, OnInit} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {Control} from '../control/control';
import {Switch} from '../control/switch';
import {ErrorMessages} from '../shared/error-messages';
import {ErrorMessageHandler} from '../shared/error-message-handler';
import {ControlService} from '../control/control-service';
import {TranslateService} from '@ngx-translate/core';
import {ControlDefaults} from '../control/control-defaults';

@Component({
  selector: 'app-control-switch',
  templateUrl: './control-switch.component.html',
  styles: []
})
export class ControlSwitchComponent implements OnInit {
  @Input()
  control: Control;
  @Input()
  applianceId: string;
  @Input()
  controlDefaults: ControlDefaults;
  switchForm: FormGroup;
  errors: { [key: string]: string } = {};
  errorMessages: ErrorMessages;
  errorMessageHandler: ErrorMessageHandler;

  constructor(private controlService: ControlService,
              private translate: TranslateService
  ) {
  }

  ngOnInit() {
    this.switchForm = this.buildSwitchFormGroup(this.control.switch_);
  }

  buildSwitchFormGroup(switch_: Switch): FormGroup {
    return new FormGroup({
      gpio: new FormControl(switch_ ? switch_.gpio : undefined, [Validators.required]),
      reverseStates: new FormControl(switch_ ? switch_.reverseStates : undefined)
    });
  }

}
