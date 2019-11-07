import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {StatusEditComponent} from './status-edit.component';
import {TranslateLoader, TranslateModule, TranslateService} from '@ngx-translate/core';
import {Observable, of} from 'rxjs';
import {ReactiveFormsModule} from '@angular/forms';
import {StatusService} from '../status/status.service';
import {Logger, Options} from '../log/logger';
import {Level} from '../log/level';
import {By} from '@angular/platform-browser';
import {FormUtil} from '../testing/form-util';
import {FakeTranslateLoader} from '../testing/fake-translate-loader';
import createSpyObj = jasmine.createSpyObj;

const translations: any = {
  'dialog.candeactivate': 'Änderungen verwerfen?',
  'ApplianceComponent.confirmDeletion': 'Wirklich löschen?'
};

describe('StatusEditComponent', () => {
  let component: StatusEditComponent;
  let fixture: ComponentFixture<StatusEditComponent>;
  let translate: TranslateService;
  let statusService: StatusService;

  // beforeEach(async(() => {
  //   statusService = createSpyObj(['suggestRuntime']);
  //
  //   TestBed.configureTestingModule({
  //     declarations: [ StatusEditComponent ],
  //     imports: [
  //       ReactiveFormsModule,
  //       TranslateModule.forRoot({
  //         loader: {
  //           provide: TranslateLoader,
  //           useValue: new FakeTranslateLoader(translations)
  //         }
  //       })
  //     ],
  //     providers: [
  //       {provide: StatusService, useValue: statusService},
  //       Logger,
  //       {provide: Options, useValue: {level: Level.DEBUG}}
  //     ]
  //   });
  //
  //   translate = TestBed.get(TranslateService);
  //   translate.use('de');
  // }));

  beforeEach(() => {
    // statusService.suggestRuntime.and.returnValue(of(1800));
    // fixture = TestBed.createComponent(StatusEditComponent);
    // component = fixture.componentInstance;
    // fixture.detectChanges();
  });

  it('should create', () => {
    // expect(component).toBeTruthy();
    // fixture.whenStable().then(() => {
    //   expect(component.switchOnForm.controls['switchOnRunningTime'].value).toEqual('00:30');
    //   expect(fixture.debugElement.query(By.css('button[type=button]')).nativeElement.disabled).toBeFalsy();
    // });
  });

  // it('should submit the form', () => {
  //   fixture.autoDetectChanges();
  //   fixture.whenStable().then(() => {
  //     FormUtil.setInputValue(fixture, 'input[name=minRunningTime]', '1:00');
  //     const startButtonElement = fixture.debugElement.query(By.css('button[type=submit]')).nativeElement;
  //     startButtonElement.click();
  //     expect(statusService.setRuntime).toHaveBeenCalledWith(3600);
  //   });
  // });
});
