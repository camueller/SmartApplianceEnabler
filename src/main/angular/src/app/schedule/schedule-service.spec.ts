import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {inject, TestBed} from '@angular/core/testing';
import {ScheduleService} from './schedule-service';
import {HTTP_INTERCEPTORS} from '@angular/common/http';
import {ErrorInterceptor} from '../shared/http-error-interceptor';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {SaeService} from '../shared/sae-service';
import {ScheduleTestdata} from './schedule-testdata';
import {ApplianceTestdata} from '../appliance/appliance-testdata';
import {Logger, Options} from '../log/logger';
import {Level} from '../log/level';

describe('ScheduleService', () => {

  beforeEach(() => TestBed.configureTestingModule({
    imports: [HttpClientTestingModule],
    providers: [
      ScheduleService,
      {
        provide: HTTP_INTERCEPTORS,
        useClass: ErrorInterceptor,
        multi: true,
      },
      Logger,
      {provide: Options, useValue: {level: Level.DEBUG}},
    ],
    schemas: [NO_ERRORS_SCHEMA],
  }));

  afterEach(inject([HttpTestingController], (httpMock: HttpTestingController) => {
    httpMock.verify();
  }));

  it('should return an empty array if the appliance has no schedules', (done: any) => {
    const service = TestBed.get(ScheduleService);
    const httpMock = TestBed.get(HttpTestingController);
    const applianceId = ApplianceTestdata.getApplianceId();
    service.getSchedules(applianceId).subscribe(
      (res) => expect(res).toEqual([]),
      () => {},
      () => { done(); }
    );
    const req = httpMock.expectOne(`${SaeService.API}/schedules?id=${applianceId}`);
    expect(req.request.method).toEqual('GET');
    req.flush('', { status: 204, statusText: 'Not content' });
  });

  it('should return a day time frame schedule', () => {
    const service = TestBed.get(ScheduleService);
    const httpMock = TestBed.get(HttpTestingController);
    const applianceId = ApplianceTestdata.getApplianceId();
    service.getSchedules(applianceId).subscribe(res => expect(res).toEqual([ScheduleTestdata.daytimeframe12345_type()]));
    const req = httpMock.expectOne(`${SaeService.API}/schedules?id=${applianceId}`);
    expect(req.request.method).toEqual('GET');
    req.flush([ScheduleTestdata.daytimeframe12345_json(true)]);
  });

  it('should return a consecutive days time frame schedule', () => {
    const service = TestBed.get(ScheduleService);
    const httpMock = TestBed.get(HttpTestingController);
    const applianceId = ApplianceTestdata.getApplianceId();
    service.getSchedules(applianceId).subscribe(res => expect(res).toEqual([ScheduleTestdata.consecutiveDaysTimeframe567_type()]));
    const req = httpMock.expectOne(`${SaeService.API}/schedules?id=${applianceId}`);
    expect(req.request.method).toEqual('GET');
    req.flush([ScheduleTestdata.consecutiveDaysTimeframe567_json(true)]);
  });

  it('should update the schedules', () => {
    const service = TestBed.get(ScheduleService);
    const httpMock = TestBed.get(HttpTestingController);
    const applianceId = ApplianceTestdata.getApplianceId();
    service.setSchedules(applianceId, [ScheduleTestdata.daytimeframe12345_type()]).subscribe(res => expect(res).toBeTruthy());
    const req = httpMock.expectOne(`${SaeService.API}/schedules?id=${applianceId}`);
    expect(req.request.method).toEqual('PUT');
    expect(JSON.parse(req.request.body)).toEqual(jasmine.objectContaining([ScheduleTestdata.daytimeframe12345_json(false)]));
  });

})
