import {inject, TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {ApplianceService} from './appliance.service';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {SaeService} from '../shared/sae-service';
import {ErrorInterceptor} from '../shared/http-error-interceptor';
import {HTTP_INTERCEPTORS} from '@angular/common/http';
import {ApplianceTestdata} from './appliance-testdata';
import {Logger, Options} from '../log/logger';
import {Level} from '../log/level';

describe('ApplianceService', () => {

  beforeEach(() => TestBed.configureTestingModule({
    imports: [HttpClientTestingModule],
    providers: [
      ApplianceService,
      {
        provide: HTTP_INTERCEPTORS,
        useClass: ErrorInterceptor,
        multi: true,
      },
      Logger,
      {provide: Options, useValue: {level: Level.DEBUG}}
    ],
    schemas: [NO_ERRORS_SCHEMA],
  }));

  afterEach(inject([HttpTestingController], (httpMock: HttpTestingController) => {
    httpMock.verify();
  }));

  it('should return an appliance', () => {
    const service = TestBed.inject(ApplianceService);
    const httpMock = TestBed.inject(HttpTestingController);
    const expectedAppliance = ApplianceTestdata.create();
    service.getAppliance(expectedAppliance.id).subscribe(res => expect(res).toEqual(expectedAppliance));
    const req = httpMock.expectOne(`${SaeService.API}/appliance?id=${expectedAppliance.id}`);
    expect(req.request.method).toEqual('GET');
    req.flush(expectedAppliance);
  });

  it(`should return empty Observable if the appliance to be retrieved is not found`, (done: any) => {
    const service = TestBed.inject(ApplianceService);
    const httpMock = TestBed.inject(HttpTestingController);
    const applianceId = ApplianceTestdata.getApplianceId();
    service.getAppliance(applianceId).subscribe(
      (res) => expect(res).toBeFalsy(),
      () => {},
      () => { done(); });
    const req = httpMock.expectOne(`${SaeService.API}/appliance?id=${applianceId}`);
    expect(req.request.method).toEqual('GET');
    req.flush('', { status: 404, statusText: 'Not found' });
  });

  xit('should update an appliance', () => {
    const service = TestBed.inject(ApplianceService);
    const httpMock = TestBed.inject(HttpTestingController);
    const appliance = ApplianceTestdata.create();
    const createNewAppliance = false;
    service.updateAppliance(appliance.id, appliance, createNewAppliance).subscribe(res => expect(res).toBeTruthy());
    const req = httpMock.expectOne(`${SaeService.API}/appliance?id=${appliance.id}&create=${createNewAppliance}`);
    expect(req.request.method).toEqual('PUT');
    expect(req.request.body).toEqual(JSON.stringify(appliance));
  });

  it(`should return empty Observable if the appliance to be updated is not found`, (done: any) => {
    const service = TestBed.inject(ApplianceService);
    const httpMock = TestBed.inject(HttpTestingController);
    const appliance = ApplianceTestdata.create();
    const createNewAppliance = false;
    service.updateAppliance('fake id', appliance, createNewAppliance).subscribe(
      (res) => expect(res).toBeFalsy(),
      () => {},
      () => { done(); });
    const req = httpMock.expectOne(`${SaeService.API}/appliance?id=${appliance.id}&create=${createNewAppliance}`);
    expect(req.request.method).toEqual('PUT');
    req.flush('', { status: 404, statusText: 'Not found' });
  });

  it('should delete an appliance', () => {
    const service = TestBed.inject(ApplianceService);
    const httpMock = TestBed.inject(HttpTestingController);
    const applianceId = ApplianceTestdata.getApplianceId();
    service.deleteAppliance(applianceId).subscribe(res => expect(res).toBeTruthy());
    const req = httpMock.expectOne(`${SaeService.API}/appliance?id=${applianceId}`);
    expect(req.request.method).toEqual('DELETE');
  });

  it(`should return empty Observable if the appliance to be deleted is not found`, (done: any) => {
    const service = TestBed.inject(ApplianceService);
    const httpMock = TestBed.inject(HttpTestingController);
    const applianceId = ApplianceTestdata.getApplianceId();
    service.deleteAppliance(applianceId).subscribe(
      (res) => expect(res).toBeFalsy(),
      () => {},
      () => { done(); });
    const req = httpMock.expectOne(`${SaeService.API}/appliance?id=${applianceId}`);
    expect(req.request.method).toEqual('DELETE');
    req.flush('', { status: 404, statusText: 'Not found' });
  });

});
