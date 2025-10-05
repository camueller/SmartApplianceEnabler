import {inject, TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {SaeService} from '../shared/sae-service';
import {ErrorInterceptor} from '../shared/http-error-interceptor';
import {HTTP_INTERCEPTORS} from '@angular/common/http';
import {SettingsService} from './settings-service';
import {SettingsTestdata} from './settings-testdata';
import {Logger, Options} from '../log/logger';
import {Level} from '../log/level';

describe('SettingsService', () => {

  beforeEach(() => TestBed.configureTestingModule({
    imports: [HttpClientTestingModule],
    providers: [
      SettingsService,
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

  it('should return settings defaults', () => {
    const service = TestBed.inject(SettingsService);
    const httpMock = TestBed.inject(HttpTestingController);
    service.getSettingsDefaults().subscribe(res => expect(res).toEqual(SettingsTestdata.settingsdefaults_type()));
    const req = httpMock.expectOne(`${SaeService.API}/settingsdefaults`);
    expect(req.request.method).toEqual('GET');
    req.flush(SettingsTestdata.settingsdefaults_json());
  });

  it('should return the empty settings', () => {
    const service = TestBed.inject(SettingsService);
    const httpMock = TestBed.inject(HttpTestingController);
    service.getSettings().subscribe(res => expect(res).toEqual(jasmine.objectContaining(SettingsTestdata.none_type())));
    const req = httpMock.expectOne(`${SaeService.API}/settings`);
    expect(req.request.method).toEqual('GET');
    req.flush(SettingsTestdata.none_json());
  });

  it('should return all settings', () => {
    const service = TestBed.inject(SettingsService);
    const httpMock = TestBed.inject(HttpTestingController);
    service.getSettings().subscribe(res => expect(res).toEqual(jasmine.objectContaining(SettingsTestdata.all_type())));
    const req = httpMock.expectOne(`${SaeService.API}/settings`);
    expect(req.request.method).toEqual('GET');
    req.flush(SettingsTestdata.all_json());
  });

});
