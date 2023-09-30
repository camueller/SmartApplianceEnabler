import {Injectable} from '@angular/core';
import { ActivatedRouteSnapshot } from '@angular/router';
import {MeterService} from './meter-service';
import {Observable} from 'rxjs';
import {MeterDefaults} from './meter-defaults';

@Injectable()
export class MeterDefaultsResolver  {

  constructor(private meterService: MeterService) {
  }

  resolve(route: ActivatedRouteSnapshot): Observable<MeterDefaults> {
    return this.meterService.getMeterDefaults();
  }
}
