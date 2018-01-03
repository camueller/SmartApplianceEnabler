import {inject, TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {SaeService} from '../shared/sae-service';
import {ErrorInterceptor} from '../shared/http-error-interceptor';
import {HTTP_INTERCEPTORS} from '@angular/common/http';
import {ControlService} from './control-service';
import {ApplianceTestdata} from '../appliance/appliance-testdata';
import {ControlTestdata} from './control-testdata';
import {Logger, Options} from '../log/logger';
import {Level} from '../log/level';

describe('ControlService', () => {

  beforeEach(() => TestBed.configureTestingModule({
    imports: [HttpClientTestingModule],
    providers: [
      ControlService,
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

  it('should return control defaults', () => {
    const service = TestBed.get(ControlService);
    const httpMock = TestBed.get(HttpTestingController);
    service.getControlDefaults().subscribe(res => expect(res).toEqual(ControlTestdata.controldefaults_type()));
    const req = httpMock.expectOne(`${SaeService.API}/controldefaults`);
    expect(req.request.method).toEqual('GET');
    req.flush(ControlTestdata.controldefaults_json());
  });

  it('should return empty Observable if the appliance has no control', (done: any) => {
    const service = TestBed.get(ControlService);
    const httpMock = TestBed.get(HttpTestingController);
    const applianceId = ApplianceTestdata.getApplianceId();
    service.getControl(applianceId).subscribe(
      (res) => expect(res).toEqual(ControlTestdata.none_undefinedtype_type()),
      () => {},
      () => { done(); }
    );
    const req = httpMock.expectOne(`${SaeService.API}/control?id=${applianceId}`);
    expect(req.request.method).toEqual('GET');
    req.flush('', { status: 204, statusText: 'Not content' });
  });

  it('should return the switch of an appliance', () => {
    const service = TestBed.get(ControlService);
    const httpMock = TestBed.get(HttpTestingController);
    const applianceId = ApplianceTestdata.getApplianceId();
    service.getControl(applianceId).subscribe(res => expect(res).toEqual(ControlTestdata.switch_type()));
    const req = httpMock.expectOne(`${SaeService.API}/control?id=${applianceId}`);
    expect(req.request.method).toEqual('GET');
    req.flush(ControlTestdata.switch_json());
  });

  it('should return the starting current switch of an appliance', () => {
    const service = TestBed.get(ControlService);
    const httpMock = TestBed.get(HttpTestingController);
    const applianceId = ApplianceTestdata.getApplianceId();
    service.getControl(applianceId).subscribe(res => expect(res).toEqual(ControlTestdata.switch_StartingCurrent_type()));
    const req = httpMock.expectOne(`${SaeService.API}/control?id=${applianceId}`);
    expect(req.request.method).toEqual('GET');
    req.flush(ControlTestdata.switch_StartingCurrent_json());
  });

  it('should update a control', () => {
    const service = TestBed.get(ControlService);
    const httpMock = TestBed.get(HttpTestingController);
    const applianceId = ApplianceTestdata.getApplianceId();
    service.updateControl(ControlTestdata.switch_type(), applianceId).subscribe(res => expect(res).toBeTruthy());
    const req = httpMock.expectOne(`${SaeService.API}/control?id=${applianceId}`);
    expect(req.request.method).toEqual('PUT');
    expect(req.request.body).toEqual(JSON.stringify(ControlTestdata.switch_json()));
  });

  it('should return empty Observable if the control to be updated is not found', (done: any) => {
    const service = TestBed.get(ControlService);
    const httpMock = TestBed.get(HttpTestingController);
    const applianceId = ApplianceTestdata.getApplianceId();
    service.updateControl(ControlTestdata.switch_type(), applianceId).subscribe(
      (res) => expect(res).toBeFalsy(),
      () => {},
      () => { done(); }
    );
    const req = httpMock.expectOne(`${SaeService.API}/control?id=${applianceId}`);
    expect(req.request.method).toEqual('PUT');
    req.flush('', { status: 404, statusText: 'Not found' });
  });

  it('should delete a control', () => {
    const service = TestBed.get(ControlService);
    const httpMock = TestBed.get(HttpTestingController);
    const applianceId = ApplianceTestdata.getApplianceId();
    service.updateControl(ControlTestdata.none_type(), applianceId).subscribe(res => expect(res).toBeTruthy());
    const req = httpMock.expectOne(`${SaeService.API}/control?id=${applianceId}`);
    expect(req.request.method).toEqual('DELETE');
  });

});
