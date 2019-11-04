import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {Component, NO_ERRORS_SCHEMA} from '@angular/core';
import {TranslateLoader, TranslateModule, TranslateService} from '@ngx-translate/core';
import {FakeTranslateLoader} from '../testing/fake-translate-loader';
import {FormGroup, FormGroupDirective, ReactiveFormsModule} from '@angular/forms';
import {Level} from '../log/level';
import {Logger, Options} from '../log/logger';
import {HttpWriteComponent} from './http-write.component';
import {ControlValueName} from '../control/control-value-name';
import {By} from '@angular/platform-browser';

const translations = require('assets/i18n/de.json');


@Component({selector: 'app-http-write-value', template: ''})
class HttpWriteValueStubComponent {}

class MockFormGroupDirective {
  form: FormGroup;
}

describe('HttpWriteComponent', () => {
  let component: HttpWriteComponent;
  let fixture: ComponentFixture<HttpWriteComponent>;
  const mockFormGroupDirective = {form: new FormGroup({})} as MockFormGroupDirective;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        HttpWriteComponent,
        HttpWriteValueStubComponent,
      ],
      imports: [
        ReactiveFormsModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useValue: new FakeTranslateLoader(translations)
          }
        })
      ],
      providers: [
        Logger,
        {provide: Options, useValue: {level: Level.DEBUG}},
        {provide: FormGroupDirective, useValue: mockFormGroupDirective}
      ],
      schemas: [ NO_ERRORS_SCHEMA ]
    });
    fixture = TestBed.createComponent(HttpWriteComponent);
    component = fixture.componentInstance;

    const translate = TestBed.get(TranslateService);
    translate.use('de');

    component.formHandler.markLabelsRequired = jest.fn();
    component.translationKeys = ['ControlHttpComponent.On', 'ControlHttpComponent.Off'];

    fixture.detectChanges();

    component.httpWrite.writeValues[0].name = ControlValueName.On;
    component.translationPrefix = 'ControlHttpComponent.';

    fixture.detectChanges();
    fixture.whenStable().then(() => {
      console.log('WRITEVALUE=', component.httpWrite.writeValues[0]);
     console.log('HTML=', fixture.debugElement.nativeElement.innerHTML);
    });
  }));

  describe('Initially', () => {

    it('a HttpWrite exists', () => {
      expect(component.httpWrite).toBeTruthy();
    });

    xit('the HttpWrite contains one HttpWriteValue', async( () => {
      expect(component.httpWrite.writeValues.length).toBe(1);
    }));

    it('URL input field exists', () => {
      expect(fixture.debugElement.query(By.css('input[ng-reflect-name="Url"]'))).toBeTruthy();
    });

  });


});
