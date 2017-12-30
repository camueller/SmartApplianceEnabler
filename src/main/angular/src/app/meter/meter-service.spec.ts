import {inject, TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {MeterService} from './meter-service';
import {HTTP_INTERCEPTORS} from '@angular/common/http';
import {ErrorInterceptor} from '../shared/http-error-interceptor';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {SaeService} from '../shared/sae-service';
import {MeterTestdata} from './meter-testdata';
import {ApplianceTestdata} from '../appliance/appliance-testdata';

describe('MeterService', () => {

  beforeEach(() => TestBed.configureTestingModule({
    imports: [HttpClientTestingModule],
    providers: [
      MeterService,
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

  it('should return meter defaults', () => {
    const service = TestBed.get(MeterService);
    const httpMock = TestBed.get(HttpTestingController);
    service.getMeterDefaults().subscribe(res => expect(res).toEqual(MeterTestdata.meterdefaults_type()));
    const req = httpMock.expectOne(`${SaeService.API}/meterdefaults`);
    expect(req.request.method).toEqual('GET');
    req.flush(MeterTestdata.meterdefaults_json());
  });

  it('should return empty Observable if the appliance has no meter', (done: any) => {
    const service = TestBed.get(MeterService);
    const httpMock = TestBed.get(HttpTestingController);
    const applianceId = ApplianceTestdata.getApplianceId();
    service.getMeter(applianceId).subscribe(
      (res) => expect(res).toEqual(MeterTestdata.none_undefinedtype_type()),
      () => {},
      () => { done(); }
    );
    const req = httpMock.expectOne(`${SaeService.API}/meter?id=${applianceId}`);
    expect(req.request.method).toEqual('GET');
    req.flush('', { status: 204, statusText: 'Not content' });
  });

  it('should return the S0ElectricityMeter of an appliance', () => {
    const service = TestBed.get(MeterService);
    const httpMock = TestBed.get(HttpTestingController);
    const applianceId = ApplianceTestdata.getApplianceId();
    service.getMeter(applianceId).subscribe(res => expect(res).toEqual(MeterTestdata.s0ElectricityMeter_type()));
    const req = httpMock.expectOne(`${SaeService.API}/meter?id=${applianceId}`);
    expect(req.request.method).toEqual('GET');
    req.flush(MeterTestdata.s0ElectricityMeter_json());
  });

  it('should update a S0ElectricityMeter', () => {
    const service = TestBed.get(MeterService);
    const httpMock = TestBed.get(HttpTestingController);
    const applianceId = ApplianceTestdata.getApplianceId();
    service.updateMeter(MeterTestdata.s0ElectricityMeter_type(), applianceId).subscribe(res => expect(res).toBeTruthy());
    const req = httpMock.expectOne(`${SaeService.API}/meter?id=${applianceId}`);
    expect(req.request.method).toEqual('PUT');
    expect(req.request.body).toEqual(JSON.stringify(MeterTestdata.s0ElectricityMeter_json()));
  });

  it('should return empty Observable if the meter to be updated is not found', (done: any) => {
    const service = TestBed.get(MeterService);
    const httpMock = TestBed.get(HttpTestingController);
    const applianceId = ApplianceTestdata.getApplianceId();
    service.updateMeter(MeterTestdata.s0ElectricityMeter_type(), applianceId).subscribe(
      (res) => expect(res).toBeFalsy(),
      () => {},
      () => { done(); }
    );
    const req = httpMock.expectOne(`${SaeService.API}/meter?id=${applianceId}`);
    expect(req.request.method).toEqual('PUT');
    req.flush('', { status: 404, statusText: 'Not found' });
  });

  it('should delete a S0ElectricityMeter', () => {
    const service = TestBed.get(MeterService);
    const httpMock = TestBed.get(HttpTestingController);
    const applianceId = ApplianceTestdata.getApplianceId();
    service.updateMeter(MeterTestdata.none_type(), applianceId).subscribe(res => expect(res).toBeTruthy());
    const req = httpMock.expectOne(`${SaeService.API}/meter?id=${applianceId}`);
    expect(req.request.method).toEqual('DELETE');
  });

});
