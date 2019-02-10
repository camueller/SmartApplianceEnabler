import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {StatusEvchargerViewComponent} from './status-evcharger-view.component';
import {TranslateLoader, TranslateModule, TranslateService} from '@ngx-translate/core';
import {FakeTranslateLoader} from '../testing/fake-translate-loader';
import {Status} from '../status/status';
import {EvStatus} from '../status/ev-status';

const translations: any = {
  'dialog.candeactivate': 'Änderungen verwerfen?',
  'ApplianceComponent.confirmDeletion': 'Wirklich löschen?'
};

describe('StatusEvchargerViewComponent', () => {
  let component: StatusEvchargerViewComponent;
  let fixture: ComponentFixture<StatusEvchargerViewComponent>;
  let translate: TranslateService;
  const defaultStatus = {
    planningRequested: true,
    evIdCharging: 1,
    evStatuses: [
      {id: 1, name: 'Nissan Leaf'} as EvStatus
    ]
  } as Status;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ StatusEvchargerViewComponent ],
      imports: [
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useValue: new FakeTranslateLoader(translations)
          }
        })
      ]
    }).compileComponents();

    translate = TestBed.get(TranslateService);
    translate.use('de');
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(StatusEvchargerViewComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    component.status = defaultStatus;
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });
});
