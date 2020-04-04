import {Subject} from 'rxjs';

export interface TrafficLightClick {

  isRedClickable(): boolean;

  onRedClicked(key: any, onActionCompleted: Subject<any>);

  isGreenClickable(): boolean;

  onGreenClicked(key: any, onActionCompleted: Subject<any>);

}
