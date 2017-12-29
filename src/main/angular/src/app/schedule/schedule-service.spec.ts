import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {inject, TestBed} from '@angular/core/testing';
import {ScheduleService} from './schedule-service';
import {HTTP_INTERCEPTORS} from '@angular/common/http';
import {ErrorInterceptor} from '../shared/http-error-interceptor';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {SaeService} from '../shared/sae-service';
import {ScheduleTestdata} from './schedule-testdata';
import {ApplianceTestdata} from '../appliance/appliance-testdata';

describe('ScheduleService', () => {

  beforeEach(() => TestBed.configureTestingModule({
    imports: [HttpClientTestingModule],
    providers: [
      ScheduleService,
      {
        provide: HTTP_INTERCEPTORS,
        useClass: ErrorInterceptor,
        multi: true,
      }
    ],
    schemas: [NO_ERRORS_SCHEMA],
  }));

  afterEach(inject([HttpTestingController], (httpMock: HttpTestingController) => {
    httpMock.verify();
  }));

  it('should return one day time frame schedule', () => {
    const service = TestBed.get(ScheduleService);
    const httpMock = TestBed.get(HttpTestingController);
    const applianceId = ApplianceTestdata.getApplianceId();
    service.getSchedules(applianceId).subscribe(res => expect(res).toEqual([ScheduleTestdata.daytimeframe12345__type()]));
    const req = httpMock.expectOne(`${SaeService.API}/schedules?id=${applianceId}`);
    expect(req.request.method).toEqual('GET');
    req.flush([ScheduleTestdata.daytimeframe12345_json()]);
  });

}
