import {inject, TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {Status} from '../status/status';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {SaeService} from '../shared/sae-service';
import {ErrorInterceptor} from '../shared/http-error-interceptor';
import {HTTP_INTERCEPTORS} from '@angular/common/http';
import {StatusService} from './status.service';
import {Logger, Options} from '../log/logger';
import {Level} from '../log/level';

describe('StatusService', () => {

  beforeEach(() => TestBed.configureTestingModule({
    imports: [HttpClientTestingModule],
    providers: [
      StatusService,
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

  it('should return the status of more than one appliance', () => {
    const service = TestBed.get(StatusService);
    const httpMock = TestBed.get(HttpTestingController);
    const expectedStatuses = [
      new Status({
        id: 'F-00000001-000000000001-00',
        name: 'WFO2842',
        type: 'WashingMachine',
        vendor: 'Bosch',
        runningTime: 1800,
        remainingMinRunningTime: 3600,
        remainingMaxRunningTime: 7200,
        planningRequested: true,
        earliestStart: 0,
        latestStart: 123,
        on: false,
        controllable: true,
        interruptedSince: null
      }),
      new Status({
        id: 'F-00000001-000000000002-00',
        name: 'SMI53M72EU',
        type: 'DishWasher',
        vendor: 'Bosch',
        runningTime: 1801,
        remainingMinRunningTime: 3601,
        remainingMaxRunningTime: 7201,
        planningRequested: true,
        earliestStart: 10,
        latestStart: 124,
        on: false,
        controllable: false,
        interruptedSince: 180
      })
    ];
    service.getStatus().subscribe(res => expect(res).toEqual(expectedStatuses));
    const req = httpMock.expectOne(`${SaeService.API}/status`);
    expect(req.request.method).toEqual('GET');
    req.flush(expectedStatuses);
  });
});
