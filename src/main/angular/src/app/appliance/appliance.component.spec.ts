import {ComponentFixture, getTestBed, TestBed} from '@angular/core/testing';
import {ApplianceComponent} from './appliance.component';
import {TranslateLoader, TranslateModule, TranslateService} from '@ngx-translate/core';
import {FormsModule} from '@angular/forms';
import {Observable} from 'rxjs/Observable';
import {DebugElement, Injector} from '@angular/core';
import {ApplianceService} from './appliance.service';
import {DialogService} from '../shared/dialog.service';
import {AppliancesReloadService} from './appliances-reload-service';
import {ActivatedRoute} from '@angular/router';
import {ActivatedRouteStub} from '../testing/router-stubs';
import {ApplianceTestdata} from './appliance-testdata';
import {RouterTestingModule} from '@angular/router/testing';
import {Appliance} from './appliance';
import {By} from '@angular/platform-browser';
import {Level} from '../log/level';
import {Logger, Options} from '../log/logger';

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
}

describe('ApplianceComponent', () => {

  let component: ApplianceComponent;
  let fixture: ComponentFixture<ApplianceComponent>;
  let location: Location;
  let activatedRoute: ActivatedRouteStub;
  let applianceService: ApplianceServiceMock;
  let dialogService: DialogService;
  let translate: TranslateService;
  let injector: Injector;
  let appliance: Appliance;

  beforeEach((async() => {
    applianceService = new ApplianceServiceMock();
    activatedRoute = new ActivatedRouteStub();
    dialogService = new DialogService();
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
        AppliancesReloadService,
        Logger,
        {provide: Options, useValue: {level: Level.DEBUG}},
      ]
    });

    injector = getTestBed();

    translate = injector.get(TranslateService);
    translate.use('de');

    appliance = ApplianceTestdata.create();

    activatedRoute.testParamMap = { id: appliance.id };
    activatedRoute.testData = { appliance: appliance };

    location = TestBed.get(Location);

    fixture = TestBed.createComponent(ApplianceComponent);
    component = fixture.componentInstance;
  }));

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should handle existing appliance', (async() => {
    fixture.detectChanges();
    fixture.whenStable().then(() => {
      expect(component.discardChangesMessage).toEqual(translations['dialog.candeactivate']);
      expect(component.confirmDeletionMessage).toEqual(translations['ApplianceComponent.confirmDeletion']);
      expect(component.appliance).toEqual(appliance);

      fixture.detectChanges();
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
});
