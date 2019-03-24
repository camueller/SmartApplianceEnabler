import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, Resolve} from '@angular/router';
import {MeterService} from './meter-service';
import {Observable} from 'rxjs';
import {MeterDefaults} from './meter-defaults';

@Injectable()
export class MeterDefaultsResolver implements Resolve<MeterDefaults> {

  constructor(private meterService: MeterService) {
  }

  resolve(route: ActivatedRouteSnapshot): Observable<MeterDefaults> {
    return this.meterService.getMeterDefaults();
  }
}
