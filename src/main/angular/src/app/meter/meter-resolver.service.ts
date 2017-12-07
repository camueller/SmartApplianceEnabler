
import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, Resolve} from '@angular/router';
import {Meter} from './meter';
import {MeterService} from './meter-service';
import {Observable} from 'rxjs/Observable';

@Injectable()
export class MeterResolver implements Resolve<Meter> {

  constructor(private meterService: MeterService) {
  }

  resolve(route: ActivatedRouteSnapshot): Observable<Meter> {
    return this.meterService.getMeter(route.params['id']);
  }
}
