import {inject, TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {ApplianceService} from './appliance.service';
import {ApplianceStatus} from './appliance-status';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {SaeService} from '../shared/sae-service';
import {Appliance} from './appliance';
import {ErrorInterceptor} from '../shared/http-error-interceptor';
import {HTTP_INTERCEPTORS} from '@angular/common/http';

describe('ApplianceService', () => {

  beforeEach(() => TestBed.configureTestingModule({
    imports: [HttpClientTestingModule],
    providers: [
      ApplianceService,
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

  it('should return an appliance', () => {
    const service = TestBed.get(ApplianceService);
    const httpMock = TestBed.get(HttpTestingController);

    const expectedAppliance = new Appliance({
      id: 'F-00000001-000000000001-00',
      name: 'WFO2842',
      type: 'WashingMachine',
      serial: '12345678',
      vendor: 'Bosch',
      maxPowerConsumption: '4000',
      currentPowerMethod: 'Measurement',
      interruptionsAllowed: true
    });

    service.getAppliance(expectedAppliance.id).subscribe(res => expect(res).toEqual(expectedAppliance));

    const req = httpMock.expectOne(`${SaeService.API}/appliance?id=${expectedAppliance.id}`);
    expect(req).toBeDefined();
    expect(req.request.method).toEqual('GET');
    req.flush(expectedAppliance);
  });

  it(`should return empty Observable for HTTP code 404 (Not found)`, (done: any) => {
    const service = TestBed.get(ApplianceService);
    const httpMock = TestBed.get(HttpTestingController);

    const unknownApplianceId = 'unknown';
    service.getAppliance(unknownApplianceId).subscribe(
      (res) => expect(res).toBeFalsy(),
      () => {},
      () => { done(); });
    httpMock.expectOne(`${SaeService.API}/appliance?id=${unknownApplianceId}`).flush(null,
      { status: 404, statusText: 'Not found' });
  });

  it('should return the status of more than one appliance', () => {
    const service = TestBed.get(ApplianceService);
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

    service.getApplianceStatus().subscribe(res => expect(res).toEqual(expectedStatuses));

    const req = httpMock.expectOne(`${SaeService.API}/status`);
    expect(req).toBeDefined();
    expect(req.request.method).toEqual('GET');
    req.flush(expectedStatuses);
  });
});
