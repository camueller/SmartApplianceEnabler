import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {ApplianceService} from './appliance.service';
import {ApplianceStatus} from './appliance-status';
import {NO_ERRORS_SCHEMA} from '@angular/core';

describe('ApplianceService', () => {

  beforeEach(() => TestBed.configureTestingModule({
    imports: [HttpClientTestingModule],
    providers: [ApplianceService],
    schemas: [NO_ERRORS_SCHEMA],
  }));

  it('should return the appliance status', () => {
    const applianceService = TestBed.get(ApplianceService);
    const http = TestBed.get(HttpTestingController);

    const expectedStatus = new ApplianceStatus();
    expectedStatus.id = 'F-00000001-000000000001-00';
    expectedStatus.name = 'WFO2842';
    expectedStatus.type = 'WashingMachine';
    expectedStatus.vendor = 'Bosch';
    expectedStatus.runningTime = 1800;
    expectedStatus.remainingMinRunningTime = 3600;
    expectedStatus.remainingMaxRunningTime = 7200;
    expectedStatus.planningRequested = true;
    expectedStatus.earliestStart = 0;
    expectedStatus.latestStart = 123;
    expectedStatus.on = false;
    expectedStatus.controllable = true;
    expectedStatus.interruptedSince = null;

    const expectedStatus2 = new ApplianceStatus();
    expectedStatus2.id = 'F-00000001-000000000002-00';
    expectedStatus2.name = 'SMI53M72EU';
    expectedStatus2.type = 'DishWasher';
    expectedStatus2.vendor = 'Bosch';
    expectedStatus2.runningTime = 1801;
    expectedStatus2.remainingMinRunningTime = 3601;
    expectedStatus2.remainingMaxRunningTime = 7201;
    expectedStatus2.planningRequested = true;
    expectedStatus2.earliestStart = 10;
    expectedStatus2.latestStart = 124;
    expectedStatus2.on = false;
    expectedStatus2.controllable = false;
    expectedStatus2.interruptedSince = 180;

    const expectedStatuses = [ expectedStatus, expectedStatus2 ];

    let actualStatus = [];
    applianceService.getApplianceStatus().subscribe((status: Array<ApplianceStatus>) => {
      actualStatus = status;
    });
    http.expectOne('http://localhost:8080/sae/status').flush(expectedStatuses);
    expect(actualStatus).toEqual(expectedStatuses);
  });
});
