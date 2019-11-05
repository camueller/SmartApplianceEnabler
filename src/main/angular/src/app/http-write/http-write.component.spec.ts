import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {Component, DebugElement, NO_ERRORS_SCHEMA} from '@angular/core';
import {TranslateLoader, TranslateModule, TranslateService} from '@ngx-translate/core';
import {FakeTranslateLoader} from '../testing/fake-translate-loader';
import {FormGroup, FormGroupDirective, ReactiveFormsModule} from '@angular/forms';
import {Level} from '../log/level';
import {Logger, Options} from '../log/logger';
import {HttpWriteComponent} from './http-write.component';
import {ControlValueName} from '../control/control-value-name';
import {By} from '@angular/platform-browser';

const translations = require('assets/i18n/de.json');

@Component({
  template: `<app-http-write (remove)="onHttpWriteRemove()" [translationKeys]="['dummy']"></app-http-write>`
})
class HttpWriteTestHostComponent {
  onHttpWriteRemoveCalled: boolean;
  onHttpWriteRemove() {
    this.onHttpWriteRemoveCalled = true;
  }
}

@Component({selector: 'app-http-write-value', template: ''})
class HttpWriteValueStubComponent {}

class MockFormGroupDirective {
  form: FormGroup;
}

describe('HttpWriteComponent Bindings', () => {
  let component: HttpWriteTestHostComponent;
  let fixture: ComponentFixture<HttpWriteTestHostComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        HttpWriteComponent,
        HttpWriteTestHostComponent
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
        {provide: FormGroupDirective, useValue: {form: new FormGroup({})} as MockFormGroupDirective}
      ],
      schemas: [ NO_ERRORS_SCHEMA ]
    });
    fixture  = TestBed.createComponent(HttpWriteTestHostComponent);
    component = fixture.componentInstance;

    const translate = TestBed.get(TranslateService);
    translate.use('de');

    fixture.detectChanges();
  }));

  it('should handle "remove" event', () => {
    fixture.detectChanges();
    fixture.whenStable().then(() => {
      // console.log('HTML=', fixture.debugElement.nativeElement.innerHTML);
      const removeButton = fixture.debugElement.query(By.css('.buttonRemoveHttpWrite'));
      removeButton.nativeElement.click();
      expect(component.onHttpWriteRemoveCalled).toBeTruthy();
    });
  });

});


describe('HttpWriteComponent', () => {
  let component: HttpWriteComponent;
  let fixture: ComponentFixture<HttpWriteComponent>;

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
        {provide: FormGroupDirective, useValue: {form: new FormGroup({})} as MockFormGroupDirective}
      ],
      schemas: [ NO_ERRORS_SCHEMA ]
    });
    fixture = TestBed.createComponent(HttpWriteComponent);
    component = fixture.componentInstance;

    const translate = TestBed.get(TranslateService);
    translate.use('de');

    component.formHandler.markLabelsRequired = jest.fn();
    component.translationKeys = ['ControlHttpComponent.On', 'ControlHttpComponent.Off'];
    component.formControlNamePrefix = 'write';

    fixture.detectChanges();

    component.httpWrite.writeValues[0].name = ControlValueName.On;
    component.translationPrefix = 'ControlHttpComponent.';

    // fixture.detectChanges();
    // fixture.whenStable().then(() => {
    //   console.log('HTML=', fixture.debugElement.nativeElement.innerHTML);
    // });
  }));

  describe('Initially', () => {

    it('a HttpWrite exists', () => {
      expect(component.httpWrite).toBeTruthy();
    });

    it('the HttpWrite contains one HttpWriteValue', async( () => {
      expect(component.httpWrite.writeValues.length).toBe(1);
    }));

  });

  describe('URL field', () => {
    let url: DebugElement;
    let urlNE: any;

    beforeEach(() => {
      url = fixture.debugElement.query(By.css('input[ng-reflect-name="writeUrl"]'));
      urlNE = url.nativeElement;
    });

    it('exists', () => {
      expect(url).toBeTruthy();
    });

    // FIXME: find from input via parent
    it('has label', () => {
      expect(fixture.debugElement.query(By.css('label'))).toBeTruthy();
    });

    it('with valid URL should make the form valid', () => {
      const inputValue = 'http://web.de';
      urlNE.value = inputValue;
      urlNE.dispatchEvent(new Event('input'));
      expect(component.form.controls['writeUrl'].value).toBe(inputValue);
      expect(component.form.valid).toBeTruthy();
    });

    it('with invalid URL should make the form invalid', () => {
      const inputValue = 'http:web.de';
      urlNE.value = inputValue;
      urlNE.dispatchEvent(new Event('input'));
      expect(component.form.controls['writeUrl'].value).toBe(inputValue);
      expect(component.form.valid).toBeFalsy();
    });

    it('with invalid URL should display an error message', () => {
      const inputValue = 'http:web.de';
      urlNE.value = inputValue;
      urlNE.dispatchEvent(new Event('input'));
      fixture.detectChanges();
      fixture.whenStable().then(() => {
        const error = fixture.debugElement.query(By.css('div.negative'));
        expect(error.nativeElement.innerHTML).toBe('Die URL muss gültig sein');
      });
    });
  });


});
