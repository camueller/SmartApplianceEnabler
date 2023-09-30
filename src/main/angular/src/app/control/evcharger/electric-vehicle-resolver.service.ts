import { ActivatedRouteSnapshot } from '@angular/router';
import {Observable} from 'rxjs';
import {Injectable} from '@angular/core';
import {ElectricVehicle} from './electric-vehicle/electric-vehicle';
import {ControlService} from '../control-service';

@Injectable()
export class ElectricVehicleResolver  {

  constructor(private controlService: ControlService) {
  }

  resolve(route: ActivatedRouteSnapshot): Observable<ElectricVehicle[]> {
    return this.controlService.getElectricVehicles(route.params['id']);
  }
}
