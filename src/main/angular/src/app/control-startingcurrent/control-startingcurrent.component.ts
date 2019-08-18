import {Component, Input, OnInit} from '@angular/core';
import {ControlDefaults} from '../control/control-defaults';
import {ControlContainer, FormControl, FormGroup, FormGroupDirective} from '@angular/forms';
import {StartingCurrentSwitch} from './starting-current-switch';

@Component({
  selector: 'app-control-startingcurrent',
  templateUrl: './control-startingcurrent.component.html',
  styleUrls: ['../global.css'],
  viewProviders: [
    {provide: ControlContainer, useExisting: FormGroupDirective}
  ]
})
export class ControlStartingcurrentComponent implements OnInit {

  @Input()
  startingCurrentSwitch: StartingCurrentSwitch;
  @Input()
  controlDefaults: ControlDefaults;
  private form: FormGroup;

  // TODO use NestedFormService
  public static updateModelFromForm(form: FormGroup, startingCurrentSwitch: StartingCurrentSwitch) {
    startingCurrentSwitch.powerThreshold = form.controls.powerThreshold.value;
    startingCurrentSwitch.startingCurrentDetectionDuration = form.controls.startingCurrentDetectionDuration.value;
    startingCurrentSwitch.finishedCurrentDetectionDuration = form.controls.finishedCurrentDetectionDuration.value;
    startingCurrentSwitch.minRunningTime = form.controls.minRunningTime.value;
  }

  constructor(private parent: FormGroupDirective) { }

  ngOnInit() {
    this.form = this.parent.form;
    this.expandParentForm(this.startingCurrentSwitch);
  }

  expandParentForm(scSwitch: StartingCurrentSwitch) {
    this.form.addControl('powerThreshold',
      new FormControl(scSwitch ? scSwitch.powerThreshold : undefined));
    this.form.addControl('startingCurrentDetectionDuration',
      new FormControl(scSwitch ? scSwitch.startingCurrentDetectionDuration : undefined));
    this.form.addControl('finishedCurrentDetectionDuration',
      new FormControl(scSwitch ? scSwitch.finishedCurrentDetectionDuration : undefined));
    this.form.addControl('minRunningTime',
      new FormControl(scSwitch ? scSwitch.minRunningTime : undefined));
  }
}
