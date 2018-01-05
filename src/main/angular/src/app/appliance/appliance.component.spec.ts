import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ApplianceComponent} from './appliance.component';
import {TranslateLoader, TranslateModule, TranslateService} from '@ngx-translate/core';
import {FormsModule} from '@angular/forms';
import {Observable} from 'rxjs/Observable';
import {ApplianceService} from './appliance.service';
import {DialogService} from '../shared/dialog.service';
import {AppliancesReloadService} from './appliances-reload-service';
import {ActivatedRoute} from '@angular/router';
import {ActivatedRouteStub} from '../testing/router-stubs';
import {ApplianceTestdata} from './appliance-testdata';
import {RouterTestingModule} from '@angular/router/testing';
import {By} from '@angular/platform-browser';
import {Level} from '../log/level';
import {Logger, Options} from '../log/logger';
import {Location} from '@angular/common';
import {Appliance} from './appliance';

const translations: any = {
  'dialog.candeactivate': 'Änderungen verwerfen?',
  'ApplianceComponent.confirmDeletion': 'Wirklich löschen?'
};

class FakeLoader implements TranslateLoader {
  getTranslation(lang: string): Observable<any> {
    return Observable.of(translations);
  }
}

class ApplianceServiceMock {
  updateAppliance(appliance: Appliance, create: boolean): Observable<any> {
    console.log('ApplianceServiceMock.updateAppliance(' + JSON.stringify(appliance) + ',' + create + ')');
    return Observable.of(true);
  }

  deleteAppliance(id: string): Observable<any> {
    console.log('ApplianceServiceMock.deleteAppliance(' + id + ')');
    return Observable.of(true);
  }
}

class ApplianceReloadServiceMock {
  reload() {}
}

class LocationMock {
  back() {}
}

describe('ApplianceComponent', () => {

  let component: ApplianceComponent;
  let fixture: ComponentFixture<ApplianceComponent>;
  let location: LocationMock;
  let activatedRoute: ActivatedRouteStub;
  let applianceService: ApplianceServiceMock;
  let applianceReloadService: ApplianceReloadServiceMock;
  let dialogService: DialogService;
  let translate: TranslateService;

  beforeEach((async() => {
    applianceService = new ApplianceServiceMock();
    applianceReloadService = new ApplianceReloadServiceMock();
    activatedRoute = new ActivatedRouteStub();
    dialogService = new DialogService();
    location = new LocationMock();
    spyOn(dialogService, 'confirm').and.returnValue(Observable.of(true));

    TestBed.configureTestingModule({
      declarations: [ ApplianceComponent ],
      imports: [
        FormsModule,
        RouterTestingModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useClass: FakeLoader
          }
        })
      ],
      providers: [
        {provide: ActivatedRoute, useValue: activatedRoute},
        {provide: Location, useValue: location},
        {provide: ApplianceService, useValue: applianceService},
        {provide: DialogService, useValue: dialogService},
        {provide: AppliancesReloadService, useValue: applianceReloadService},
        Logger,
        {provide: Options, useValue: {level: Level.DEBUG}},
      ]
    });

    translate = TestBed.get(TranslateService);
    translate.use('de');
  }));

  it('should be created', (async() => {
    activatedRoute.testParamMap = {};
    activatedRoute.testData = {};
    fixture = TestBed.createComponent(ApplianceComponent);
    component = fixture.componentInstance;
    expect(component).toBeTruthy();
    fixture.detectChanges();
    fixture.whenStable().then(() => {
      expect(component.discardChangesMessage).toEqual(translations['dialog.candeactivate']);
      expect(component.confirmDeletionMessage).toEqual(translations['ApplianceComponent.confirmDeletion']);
    });
  }));

  it('should provide an empty form for a new appliance', (async() => {
    activatedRoute.testParamMap = {};
    activatedRoute.testData = {};
    fixture = TestBed.createComponent(ApplianceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    fixture.whenStable().then(() => {
      expect(component.isNew).toBeTruthy();
      expect(component.detailsForm.controls['id'].value).toBeFalsy();
      expect(fixture.debugElement.query(By.css('input[name=id]')).nativeElement.placeholder)
        .toEqual('F-00000000-000000000000-00');
      expect(component.detailsForm.controls['vendor'].value).toBeFalsy();
      expect(component.detailsForm.controls['name'].value).toBeFalsy();
      expect(component.detailsForm.controls['type'].value).toBeFalsy();
      expect(component.detailsForm.controls['serial'].value).toBeFalsy();
      expect(component.detailsForm.controls['maxPowerConsumption'].value).toBeFalsy();
      expect(component.detailsForm.controls['currentPowerMethod'].value).toBeFalsy();
      expect(component.detailsForm.controls['interruptionsAllowed'].value).toBeFalsy();
      expect(fixture.debugElement.query(By.css('button[type=submit]')).nativeElement.disabled).toBeTruthy();
      expect(fixture.debugElement.query(By.css('button[type=button]')).nativeElement.disabled).toBeTruthy();
    });
  }));

  it('should allow to save a new appliance', (async() => {
    activatedRoute.testParamMap = {};
    activatedRoute.testData = {};
    fixture = TestBed.createComponent(ApplianceComponent);
    component = fixture.componentInstance;
    fixture.autoDetectChanges();
    fixture.whenStable().then(() => {
      const form = component.detailsForm.form;
      form.setValue({
        id: 'F-00000000-000000000001-01',
        vendor: 'Siemens',
        name: 'SuperWash',
        type: 'WashingMachine',
        serial: '345678',
        maxPowerConsumption: 1800,
        currentPowerMethod: 'Measurement',
        interruptionsAllowed: true
      });

      // Programmatic changes to a control's value will not mark it dirty.
      // (https://angular.io/api/forms/AbstractControl#pristine)
      form.markAsDirty();
      fixture.detectChanges();

      expect(form.valid).toBeTruthy('because completed form is valid');
      expect(form.pristine).toBeFalsy('because completed form is not pristine');
      expect(JSON.stringify(component.errors)).toEqual('{}');
      expect(fixture.debugElement.query(By.css('button[type=submit]')).nativeElement.disabled).toBeFalsy();
      expect(fixture.debugElement.query(By.css('button[type=button]')).nativeElement.disabled).toBeTruthy();
    });
  }));

  it('should provide a form containing details of existing appliance', (async() => {
    const appliance = ApplianceTestdata.create();
    activatedRoute.testParamMap = { id: appliance.id };
    activatedRoute.testData = { appliance: appliance };
    fixture = TestBed.createComponent(ApplianceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    fixture.whenStable().then(() => {
      expect(component.appliance).toEqual(appliance);
      expect(component.detailsForm.controls['id'].value).toEqual(appliance.id);
      expect(component.detailsForm.controls['vendor'].value).toEqual(appliance.vendor);
      expect(component.detailsForm.controls['name'].value).toEqual(appliance.name);
      expect(component.detailsForm.controls['type'].value).toEqual(appliance.type);
      expect(component.detailsForm.controls['serial'].value).toEqual(appliance.serial);
      expect(component.detailsForm.controls['maxPowerConsumption'].value).toEqual(appliance.maxPowerConsumption);
      expect(component.detailsForm.controls['currentPowerMethod'].value).toEqual(appliance.currentPowerMethod);
      expect(component.detailsForm.controls['interruptionsAllowed'].value).toEqual(appliance.interruptionsAllowed);
      expect(fixture.debugElement.query(By.css('button[type=submit]')).nativeElement.disabled).toBeTruthy();
      expect(fixture.debugElement.query(By.css('button[type=button]')).nativeElement.disabled).toBeFalsy();
    });
  }));

  it('should allow to update an existing appliance', (async() => {
    const appliance = ApplianceTestdata.create();
    activatedRoute.testParamMap = { id: appliance.id };
    activatedRoute.testData = { appliance: appliance };
    fixture = TestBed.createComponent(ApplianceComponent);
    component = fixture.componentInstance;
    spyOn(applianceService, 'updateAppliance').and.callThrough();
    spyOn(applianceReloadService, 'reload').and.callThrough();
    fixture.autoDetectChanges();
    fixture.whenStable().then(() => {
      expect(component.appliance).toEqual(appliance);
      const saveButtonElement = fixture.debugElement.query(By.css('button[type=submit]')).nativeElement;
      expect(saveButtonElement.disabled).toBeTruthy();
      appliance.name = 'MegaWash';
      setInputValue('input[name=name]', appliance.name);
      fixture.detectChanges();
      expect(saveButtonElement.disabled).toBeFalsy();
      saveButtonElement.click();
      expect(applianceService.updateAppliance).toHaveBeenCalledWith(appliance, false);
      expect(applianceReloadService.reload).toHaveBeenCalledTimes(1);
    });
  }));

  it('should allow to delete an existing appliance', (async() => {
    const appliance = ApplianceTestdata.create();
    activatedRoute.testParamMap = { id: appliance.id };
    activatedRoute.testData = { appliance: appliance };
    fixture = TestBed.createComponent(ApplianceComponent);
    component = fixture.componentInstance;
    spyOn(applianceService, 'deleteAppliance').and.callThrough();
    spyOn(applianceReloadService, 'reload').and.callThrough();
    spyOn(location, 'back').and.callThrough();
    fixture.detectChanges();
    fixture.whenStable().then(() => {
      expect(component.appliance).toEqual(appliance);
      const deleteButtonElement = fixture.debugElement.query(By.css('button[type=button]')).nativeElement;
      expect(deleteButtonElement.disabled).toBeFalsy();
      deleteButtonElement.click();
      expect(applianceService.deleteAppliance).toHaveBeenCalledWith(appliance.id);
      expect(applianceReloadService.reload).toHaveBeenCalledTimes(1);
      expect(location.back).toHaveBeenCalledTimes(1);
    });
  }));

  it('should complain if required values are missing', (async() => {
    activatedRoute.testParamMap = {};
    activatedRoute.testData = {};
    fixture = TestBed.createComponent(ApplianceComponent);
    component = fixture.componentInstance;
    fixture.autoDetectChanges();
    fixture.whenStable().then(() => {
      setInputValue('input[name=id]', 'F-00000000-000000000001-01');
      expect(component.detailsForm.controls.id.valid).toBeTruthy();
      expect(component.detailsForm.valid).toBeFalsy();
      setInputValue('input[name=vendor]', 'Siemens');
      expect(component.detailsForm.controls.vendor.valid).toBeTruthy();
      expect(component.detailsForm.valid).toBeFalsy();
      setInputValue('input[name=name]', 'SuperWash');
      expect(component.detailsForm.controls.name.valid).toBeTruthy();
      expect(component.detailsForm.valid).toBeFalsy();
      selectOptionValue('select[name=type]', 'WashingMachine');
      expect(component.detailsForm.controls.type.valid).toBeTruthy();
      expect(component.detailsForm.valid).toBeFalsy();
      setInputValue('input[name=serial]', '345678');
      expect(component.detailsForm.controls.serial.valid).toBeTruthy();
      expect(component.detailsForm.valid).toBeFalsy();
      setInputValue('input[name=maxPowerConsumption]', '1800');
      expect(component.detailsForm.controls.maxPowerConsumption.valid).toBeTruthy();
      expect(component.detailsForm.valid).toBeFalsy();
      selectOptionValue('select[name=currentPowerMethod]', 'Measurement');
      expect(component.detailsForm.valid).toBeTruthy();
    });
  }));

  it('should only accept valid id', (async() => {
    activatedRoute.testParamMap = {};
    activatedRoute.testData = {};
    fixture = TestBed.createComponent(ApplianceComponent);
    component = fixture.componentInstance;
    fixture.autoDetectChanges();
    fixture.whenStable().then(() => {
      setInputValue('input[name=id]', 'F-00000000-000000000001-0');
      expect(component.detailsForm.controls.id.valid).toBeFalsy();
      expect(component.errors['id']).toEqual('ApplianceComponent.error.id_pattern');
      setInputValue('input[name=id]', 'F-00000000-000000000001-01');
      expect(component.detailsForm.controls.id.valid).toBeTruthy();
      expect(JSON.stringify(component.errors)).toEqual('{}');
    });
  }));

  function setInputValue(selector: string, value: string) {
    fixture.detectChanges();
    const input = fixture.debugElement.query(By.css(selector)).nativeElement;
    input.value = value;
    input.dispatchEvent(new Event('input'));
  }

  function selectOptionValue(selector: string, value: string) {
    fixture.detectChanges();
    const input = fixture.debugElement.query(By.css(selector)).nativeElement;
    input.value = value;
    input.dispatchEvent(new Event('change'));
  }

});
