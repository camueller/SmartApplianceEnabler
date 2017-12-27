import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {ApplianceService} from './appliance.service';
import {ApplianceStatus} from './appliance-status';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {SaeService} from '../shared/sae-service';

describe('ApplianceService', () => {

  beforeEach(() => TestBed.configureTestingModule({
    imports: [HttpClientTestingModule],
    providers: [ApplianceService],
    schemas: [NO_ERRORS_SCHEMA],
  }));

  it('should return the status of more than one appliance', () => {
    const applianceService = TestBed.get(ApplianceService);
    const httpMock = TestBed.get(HttpTestingController);

    const expectedStatuses = [
      new ApplianceStatus({
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
      new ApplianceStatus({
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

    applianceService.getApplianceStatus().subscribe(res => expect(res).toEqual(expectedStatuses));

    const req = httpMock.expectOne(`${SaeService.API}/status`);
    expect(req).toBeDefined();
    expect(req.request.method).toEqual('GET');
    req.flush(expectedStatuses);
    httpMock.verify();
  });
});
