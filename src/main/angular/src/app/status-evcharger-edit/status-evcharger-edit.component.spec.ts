import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { StatusEvchargerEditComponent } from './status-evcharger-edit.component';
import {TranslateLoader, TranslateModule, TranslateService} from '@ngx-translate/core';
import {Observable, of} from 'rxjs';
import {ReactiveFormsModule} from '@angular/forms';
import {StatusService} from '../status/status.service';
import {Logger, Options} from '../log/logger';
import {Level} from '../log/level';
import {SuiModule} from 'ng2-semantic-ui';
import {Status} from '../status/status';
import {EvStatus} from '../status/ev-status';
import {FakeTranslateLoader} from '../testing/fake-translate-loader';
import {FormUtil} from '../testing/form-util';
import {By} from '@angular/platform-browser';
import createSpyObj = jasmine.createSpyObj;
import SpyObj = jasmine.SpyObj;
import {TimeUtil} from '../shared/time-util';

const translations: any = {
  'StatusComponent.stateOfCharge': 'Ladezustand: ',
  'StatusComponent.stateOfChargeCurrent': 'Ist ',
  'StatusComponent.stateOfChargeRequested': 'Soll ',
  'StatusComponent.stateOfChargeUnit': '%',
  'StatusComponent.chargeLatestEndRequested': 'bis ',
  'StatusComponent.buttonStart': 'Starten',
// 'daysOfWeek_monday': 'Montag',
  // 'daysOfWeek_tuesday': 'Dienstag',
  // 'daysOfWeek_wednesday': 'Mittwoch',
  // 'daysOfWeek_thursday': 'Donnerstag',
  // 'daysOfWeek_friday': 'Freitag',
  // 'daysOfWeek_saturday': 'Samstag',
  // 'daysOfWeek_sunday': 'Sonntag',
  // 'daysOfWeek_holiday': 'Feiertag',
};

class StatusServiceMock extends StatusService {}

describe('StatusEvchargerEditComponent', () => {
  let component: StatusEvchargerEditComponent;
  let fixture: ComponentFixture<StatusEvchargerEditComponent>;
  let translate: TranslateService;
  let statusService: SpyObj<any>;
  const defaultSoc = '44';

  beforeEach(async(() => {
    statusService = createSpyObj(['getSoc', 'requestEvCharge']);

    TestBed.configureTestingModule({
      declarations: [ StatusEvchargerEditComponent ],
      imports: [
        ReactiveFormsModule,
        SuiModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useValue: new FakeTranslateLoader(translations)
          }
        })
      ],
      providers: [
        {provide: StatusService, useValue: statusService},
        Logger,
        {provide: Options, useValue: {level: Level.DEBUG}}
      ]
    }).compileComponents();

    translate = TestBed.get(TranslateService);
    translate.use('de');
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(StatusEvchargerEditComponent);
    component = fixture.componentInstance;
    component.dows = [
      {id: 1, name: 'Monday'},
      {id: 2, name: 'Tuesday'},
      {id: 3, name: 'Wednesday'},
      {id: 4, name: 'Thursday'},
      {id: 5, name: 'Friday'},
      {id: 6, name: 'Saturday'},
      {id: 7, name: 'Sunday'},
      {id: 8, name: 'Holiday'},
    ];
    component.applianceId = 'myApplianceId';
    component.status = {
      evStatuses: [{id: 1, name: 'Nissan Leaf'} as EvStatus]
    } as Status;
  });

  it('should create', () => {
    statusService.getSoc.and.returnValue(of(undefined));
    fixture.detectChanges();
    expect(component).toBeTruthy();
    fixture.whenStable().then(() => {
      expect(component.startChargeForm.controls['stateOfChargeCurrent'].value).toEqual('');
      expect(fixture.debugElement.query(By.css('button[type=submit]')).nativeElement.disabled).toBeFalsy();
    });
  });

  it('should submit the form with current SOC', () => {
    statusService.getSoc.and.returnValue(of(defaultSoc));
    statusService.requestEvCharge.and.returnValue( of(true));
    fixture.detectChanges();
    fixture.whenStable().then(() => {
      const socCurrent = '33';
      FormUtil.setInputValue(fixture, 'input[name=stateOfChargeCurrent]', socCurrent);
      const startButtonElement = fixture.debugElement.query(By.css('button[type=submit]')).nativeElement;
      startButtonElement.click();
      expect(statusService.requestEvCharge)
        .toHaveBeenCalledWith(component.applianceId, component.status.evStatuses[0].id, socCurrent, undefined, undefined);
    });
  });

  it('should submit the form with requested SOC', () => {
    statusService.getSoc.and.returnValue(of(''));
    statusService.requestEvCharge.and.returnValue( of(true));
    fixture.detectChanges();
    fixture.whenStable().then(() => {
      const socRequested = '55';
      FormUtil.setInputValue(fixture, 'input[name=stateOfChargeRequested]', socRequested);
      const startButtonElement = fixture.debugElement.query(By.css('button[type=submit]')).nativeElement;
      startButtonElement.click();
      expect(statusService.requestEvCharge)
        .toHaveBeenCalledWith(component.applianceId, component.status.evStatuses[0].id, null, socRequested, undefined);
    });
  });

  it('should submit the form with charge end', (done: any) => {
    statusService.getSoc.and.returnValue(of(''));
    statusService.requestEvCharge.and.returnValue( of(true));
    fixture.autoDetectChanges();

    const chargeEndDow = 4; // Thursday
    const chargeEndTime = '15:00';
    const chargeEnd = TimeUtil.timestringOfNextMatchingDow(chargeEndDow, chargeEndTime);


    fixture.whenStable().then(() => {
      FormUtil.selectOption(fixture, 'chargeEndDow', 'Thursday');
      FormUtil.setInputValue(fixture, 'input[name=chargeEndTime]', chargeEndTime);

      fixture.whenStable().then(() => {
        // sui-select[formControlName='chargeEndDow']>div.text>span:nth-child(2)
        const startButtonElement = fixture.debugElement.query(By.css('button[type=submit]')).nativeElement;
        startButtonElement.click();
        expect(statusService.requestEvCharge)
          .toHaveBeenCalledWith(component.applianceId, component.status.evStatuses[0].id, null, null, chargeEnd);
        done();
      });
    });
  });
});
