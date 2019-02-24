import {Component, Input, OnInit} from '@angular/core';
import {ControlDefaults} from '../control/control-defaults';
import {FormControl, FormGroup} from '@angular/forms';
import {StartingCurrentSwitch} from './starting-current-switch';

@Component({
  selector: 'app-control-startingcurrent',
  templateUrl: './control-startingcurrent.component.html',
  styles: []
})
export class ControlStartingcurrentComponent implements OnInit {

  @Input()
  startingCurrentSwitch: StartingCurrentSwitch;
  @Input()
  controlDefaults: ControlDefaults;
  @Input()
  parentForm: FormGroup;

  constructor() { }

  ngOnInit() {
    this.expandParentForm(this.startingCurrentSwitch);
  }

  expandParentForm(scSwitch: StartingCurrentSwitch) {
    this.parentForm.addControl('powerThreshold',
      new FormControl(scSwitch ? scSwitch.powerThreshold : undefined));
    this.parentForm.addControl('startingCurrentDetectionDuration',
      new FormControl(scSwitch ? scSwitch.startingCurrentDetectionDuration : undefined));
    this.parentForm.addControl('finishedCurrentDetectionDuration',
      new FormControl(scSwitch ? scSwitch.finishedCurrentDetectionDuration : undefined));
    this.parentForm.addControl('minRunningTime',
      new FormControl(scSwitch ? scSwitch.minRunningTime : undefined));
  }
}
