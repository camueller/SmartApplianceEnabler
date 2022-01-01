import { Component, OnInit } from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {FlowExportContentComponent} from '../flow-export-content/flow-export-content.component';

@Component({
  selector: 'app-flow-export',
  templateUrl: './flow-export.component.html',
  styles: [
  ]
})
export class FlowExportComponent {

  constructor(public dialog: MatDialog) {}

  openDialog() {
    this.dialog.open(FlowExportContentComponent);
  }
}
