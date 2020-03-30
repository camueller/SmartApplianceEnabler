import {Component, Input, OnInit} from '@angular/core';

export enum MessageBoxLevel {
  INFO,
  WARN,
  ERROR
}

@Component({
  selector: 'messagebox',
  templateUrl: './messagebox.component.html',
  styleUrls: ['./messagebox.component.css']
})
export class MessageboxComponent implements OnInit {

  @Input()
  level: MessageBoxLevel = MessageBoxLevel.INFO;

  constructor() {
  }

  ngOnInit() {
  }

  get isLevelInfo() {
    return this.level === MessageBoxLevel.INFO;
  }

  get isLevelWarn() {
    return this.level === MessageBoxLevel.WARN;
  }

  get isLevelError() {
    return this.level === MessageBoxLevel.ERROR;
  }


  get icon() {
    switch (this.level) {
      case MessageBoxLevel.INFO:
        return 'info';
      case MessageBoxLevel.WARN:
        return 'warning';
      case MessageBoxLevel.ERROR:
        return 'error';
    }
  }
}
