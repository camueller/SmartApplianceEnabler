import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {ControlHttpComponent} from './control-http.component';
import {Component, DebugElement, NO_ERRORS_SCHEMA} from '@angular/core';
import {TranslateLoader, TranslateModule, TranslateService} from '@ngx-translate/core';
import {FakeTranslateLoader} from '../testing/fake-translate-loader';
import {FormGroup, FormGroupDirective, ReactiveFormsModule} from '@angular/forms';
import {Level} from '../log/level';
import {Logger, Options} from '../log/logger';
import {By} from '@angular/platform-browser';
import {HttpWriteValue} from '../http-write-value/http-write-value';
import {HttpWrite} from '../http-write/http-write';
const translations = require('assets/i18n/de.json');


@Component({selector: 'app-http-configuration', template: ''})
class HttpConfigurationStubComponent {}

@Component({selector: 'app-http-read', template: ''})
class HttpReadStubComponent {}

@Component({selector: 'app-http-write', template: ''})
class HttpWriteStubComponent {}

class MockFormGroupDirective {
  form: FormGroup;
}

describe('ControlHttpComponent', () => {
  let component: ControlHttpComponent;
  let fixture: ComponentFixture<ControlHttpComponent>;
  const controlService = jest.fn();
  const mockFormGroupDirective = {form: new FormGroup({})} as MockFormGroupDirective;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        ControlHttpComponent,
        HttpConfigurationStubComponent,
        HttpReadStubComponent,
        HttpWriteStubComponent
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
    fixture = TestBed.createComponent(ControlHttpComponent);
    component = fixture.componentInstance;

    const translate = TestBed.get(TranslateService);
    translate.use('de');

    fixture.detectChanges();
  }));

  describe('Initially', () => {

    it('there is one HttpWrite', async( () => {
      expect(component.httpSwitch.httpWrites.length).toBe(1);
    }));

  });

  describe('Button "Weitere URL"', () => {
    let button;

    beforeEach(() => {
      button = buttonAddHttpWrite();
    });

    it('exists and is enabled', async( () => {
      expect(button.nativeElement.innerHTML).toBe('Weitere URL');
      expect(button.nativeElement.disabled).toBeFalsy();
    }));

    describe('with one existing HttpWrite', () => {
      it('another HttpRead can be added if it has one HttpReadValue', async( () => {
        button.triggerEventHandler('click', null);
        expect(component.httpSwitch.httpWrites.length).toBe(2);
      }));

      it('no HttpRead can be added if the existing HttpWrite contains two HttpWriteValue', async( () => {
        component.httpSwitch.httpWrites[0].writeValues.push(new HttpWriteValue());
        fixture.detectChanges();
        expect(buttonAddHttpWrite()).toBeFalsy();
      }));
    });

    describe('with two existing HttpWrite', () => {
      it('no HttpRead can be added if the existing HttpWrite contains two HttpWriteValue', async( () => {
        component.httpSwitch.httpWrites.push(new HttpWrite());
        fixture.detectChanges();
        expect(buttonAddHttpWrite()).toBeFalsy();
      }));
    });
  });

  function buttonAddHttpWrite(): DebugElement {
    return fixture.debugElement.query(By.css('button'));
  }
});
