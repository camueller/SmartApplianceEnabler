
import {Injectable} from '@angular/core';
import { ActivatedRouteSnapshot } from '@angular/router';
import {Meter} from './meter';
import {MeterService} from './meter-service';
import {Observable} from 'rxjs';

@Injectable()
export class MeterResolver  {

  constructor(private meterService: MeterService) {
  }

  resolve(route: ActivatedRouteSnapshot): Observable<Meter> {
    return this.meterService.getMeter(route.params['id']);
  }
}
