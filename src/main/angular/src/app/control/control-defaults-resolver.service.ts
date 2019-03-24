import {ActivatedRouteSnapshot, Resolve} from '@angular/router';
import {ControlService} from './control-service';
import {Observable} from 'rxjs';
import {Injectable} from '@angular/core';
import {ControlDefaults} from './control-defaults';

@Injectable()
export class ControlDefaultsResolver implements Resolve<ControlDefaults> {

  constructor(private controlService: ControlService) {
  }

  resolve(route: ActivatedRouteSnapshot): Observable<ControlDefaults> {
    return this.controlService.getControlDefaults();
  }
}
