import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {StatusViewComponent} from './status-view.component';
import {TranslateLoader, TranslateModule, TranslateService} from '@ngx-translate/core';
import {FakeTranslateLoader} from '../testing/fake-translate-loader';
import {Status} from '../status/status';

const translations: any = {
};

describe('StatusViewComponent', () => {
  let component: StatusViewComponent;
  let fixture: ComponentFixture<StatusViewComponent>;
  let translate: TranslateService;
  const defaultStatus = {
    planningRequested: true,
  } as Status;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ StatusViewComponent ],
      imports: [
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useValue: new FakeTranslateLoader(translations)
          }
        })
      ]
    })
    .compileComponents();

    translate = TestBed.get(TranslateService);
    translate.use('de');
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(StatusViewComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    component.status = defaultStatus;
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });
});
