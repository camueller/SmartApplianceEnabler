import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {ControlService} from '../control/control-service';
import {Control} from '../control/control';
import {AlwaysOnSwitch} from './always-on-switch';
import {AppliancesReloadService} from '../appliance/appliances-reload-service';

@Component({
  selector: 'app-control-alwayson',
  templateUrl: './control-alwayson.component.html',
  styles: []
})
export class ControlAlwaysonComponent implements OnInit {
  @Input()
  control: Control;
  @Input()
  applianceId: string;
  @Output()
  childFormChanged = new EventEmitter<boolean>();
  form: FormGroup;

  constructor(
    private controlService: ControlService,
    private appliancesReloadService: AppliancesReloadService,
  ) {}

  ngOnInit() {
    this.form =  new FormGroup({});
    this.form.markAsDirty();
  }

  submitForm() {
    this.control.alwaysOnSwitch = new AlwaysOnSwitch();
    this.controlService.updateControl(this.control, this.applianceId).subscribe(
      () => this.appliancesReloadService.reload());
    this.form.markAsPristine();
    this.childFormChanged.emit(this.form.valid);
  }
}
