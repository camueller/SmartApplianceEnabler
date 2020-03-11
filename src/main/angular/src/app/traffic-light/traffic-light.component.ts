import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {TrafficLightState} from './traffic-light-state';
import {TrafficLightClick} from './traffic-light-click';
import {Subject} from 'rxjs';

@Component({
  selector: 'app-traffic-light',
  templateUrl: './traffic-light.component.html',
  styleUrls: ['./traffic-light.css', '../global.css']
})
export class TrafficLightComponent implements OnChanges, OnInit {
  @Input()
  key: any;
  @Input()
  clickHandler: TrafficLightClick;
  @Input()
  stateHandler: TrafficLightState;
  clickActionCompleted: Subject<any> = new Subject();
  showLoadingIndicator: boolean;

  constructor() {
    this.clickActionCompleted.subscribe(() => this.showLoadingIndicator = false);
  }

  ngOnChanges(changes: SimpleChanges): void {
  }

  ngOnInit() {
  }

  isRed(): boolean {
    return this.stateHandler.isRed();
  }

  isRedClickable(): boolean {
    return this.clickHandler ? this.clickHandler.isRedClickable() : false;
  }

  onRedClick() {
    this.showLoadingIndicator = true;
    this.clickHandler.onRedClicked(this.key, this.clickActionCompleted);
  }

  isYellow(): boolean {
    return this.stateHandler.isYellow();
  }

  isGreen(): boolean {
    return this.stateHandler.isGreen();
  }

  isGreenClickable(): boolean {
    return this.clickHandler ? this.clickHandler.isGreenClickable() : false;
  }

  onGreenClick() {
    this.showLoadingIndicator = true;
    this.clickHandler.onGreenClicked(this.key, this.clickActionCompleted);
  }

  isGreenBlink(): boolean {
    return this.stateHandler.isGreenBlink();
  }

}
