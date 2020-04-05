import {async, ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {Component, DebugElement, NO_ERRORS_SCHEMA, ViewChild} from '@angular/core';
import {HttpWriteComponent} from './http-write.component';
import {ControlValueName} from '../../control/control-value-name';
import {
  click,
  createComponentAndConfigure,
  debugElementByCss,
  enterAndCheckInputValue,
  importsFormsAndTranslate,
  providers
} from '../../shared/test-util';
import {FormGroup} from '@angular/forms';
import {HttpWrite} from './http-write';
import {HttpWriteValue} from '../write-value/http-write-value';

const httpWriteValueUpdateModelFromFormMock = jest.fn();

@Component({selector: 'app-http-write-value', template: ''})
class HttpWriteValueStubComponent {
  updateModelFromForm(): HttpWriteValue | undefined {
    return httpWriteValueUpdateModelFromFormMock();
  }
}

@Component({
  template: `<app-http-write
    [form]="form"
    [httpWrite]="httpWrite"
    (remove)="onHttpWriteRemove()"
  ></app-http-write>`
})
class HttpWriteTestHostComponent {
  @ViewChild(HttpWriteComponent, { static: true }) testComponent;
  onHttpWriteRemoveCalled: boolean;
  form = new FormGroup({});
  httpWrite = HttpWrite.createWithSingleChild();

  onHttpWriteRemove() {
    this.onHttpWriteRemoveCalled = true;
  }
}

describe('HttpWriteComponent', () => {

  const URL_INPUT = 'input[formcontrolname="url"]';
  const URL_LABEL = 'label.url';
  const ERROR_ELEMENT = 'div.negative';
  const ADD_HTTPWRITE_BUTTON = 'button.addValue';
  const REMOVE_HTTPWRITE_BUTTON = 'i[ng-reflect-ng-class=removeValue0]';

  let component: HttpWriteComponent;
  let hostComponent: HttpWriteTestHostComponent;
  let fixture: ComponentFixture<HttpWriteTestHostComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        HttpWriteComponent,
        HttpWriteTestHostComponent,
        HttpWriteValueStubComponent,
      ],
      imports: importsFormsAndTranslate(),
      providers: providers(),
      schemas: [NO_ERRORS_SCHEMA]
    });
    fixture = createComponentAndConfigure(HttpWriteTestHostComponent);
    hostComponent = fixture.componentInstance;
    component = hostComponent.testComponent;

    component.translationKeys = ['ControlHttpComponent.On', 'ControlHttpComponent.Off'];

    fixture.detectChanges();

    component.httpWrite.writeValues[0].name = ControlValueName.On;
    component.translationPrefix = 'ControlHttpComponent.';

    fixture.detectChanges();
    // fixture.whenStable().then(() => {
    //   console.log('HTML=', fixture.debugElement.nativeElement.innerHTML);
    // });
  }));

  describe('bindings', () => {
    it('should emit "remove" event', () => {
      fixture.detectChanges();
      fixture.whenStable().then(() => {
        debugElementByCss(fixture, '.buttonRemoveHttpWrite').nativeElement.click();
        expect(hostComponent.onHttpWriteRemoveCalled).toBeTruthy();
      });
    });
  });


  describe('initially', () => {
    it('a HttpWrite exists', () => {
      expect(component.httpWrite).toBeTruthy();
    });

    it('the HttpWrite contains one HttpWriteValue', async(() => {
      expect(component.httpWrite.writeValues.length).toBe(1);
    }));
  });

  describe('fields', () => {
    describe('url', () => {
      let url: DebugElement;

      beforeEach(() => {
        url = debugElementByCss(fixture, URL_INPUT);
      });

      it('exists', () => {
        expect(url).toBeTruthy();
      });

      it('has label', () => {
        expect(debugElementByCss(fixture, URL_LABEL)).toBeTruthy();
      });

      it('a valid value entered results in a valid form control', () => {
        enterAndCheckInputValue(component.form, 'url', url, 'http://web.de');
        expect(component.form.controls.url.valid).toBeTruthy();
      });

      it('an invalid value entered results in a invalid form control', () => {
        enterAndCheckInputValue(component.form, 'url', url, 'http:web.de');
        expect(component.form.controls.url.valid).toBeFalsy();
      });

      it('with invalid URL should display an error message', () => {
        enterAndCheckInputValue(component.form, 'url', url, 'http:web.de');
        fixture.detectChanges();
        fixture.whenStable().then(() => {
          expect(debugElementByCss(fixture, ERROR_ELEMENT).nativeElement.innerHTML).toBe('Die URL muss gültig sein');
        });
      });
    });
  });

  describe('buttons', () => {
    describe('Weiterer Zustand/Aktion', () => {
      beforeEach(() => {
        expect(component.httpWrite.writeValues.length).toBe(1);
        expect(component.form.dirty).toBeFalsy();
        click(debugElementByCss(fixture, ADD_HTTPWRITE_BUTTON));
      });

      it('should add a HttpWriteValue', () => {
        expect(component.httpWrite.writeValues.length).toBe(2);
      });

      it('set form to dirty', () => {
        fixture.whenStable().then(() => {
          expect(component.form.dirty).toBeTruthy();
        });
      });
    });

    describe('X (Remove HttpWriteValue)', () => {
      beforeEach(() => {
        expect(component.form.dirty).toBeFalsy();
        click(debugElementByCss(fixture, REMOVE_HTTPWRITE_BUTTON));
        fixture.detectChanges();
      });

      it('should remove HttpWriteValue', () => {
        expect(component.httpWrite.writeValues.length).toBe(0);
      });

      it('set form to dirty', () => {
        fixture.whenStable().then(() => {
          expect(component.form.dirty).toBeTruthy();
        });
      });
    });
  });

  describe('form', () => {
    it('filling all required inputs results in a valid form', () => {
      expect(component.form.valid).toBeFalsy();
      enterAndCheckInputValue(component.form, 'url', debugElementByCss(fixture, URL_INPUT),
        'http://web.de');
      expect(component.form.valid).toBeTruthy();
    });
  });

  describe('updateModelFromForm', () => {

    describe('returns HttpWrite', () => {
      const inputValue = 'http://web.de';
      const httpWriteValueName = 'httpWriteValue.name';
      let httpWrite: HttpWrite;

      beforeEach(fakeAsync(() => {
        httpWriteValueUpdateModelFromFormMock.mockReturnValue({name: httpWriteValueName} as HttpWriteValue);
        enterAndCheckInputValue(component.form, 'url', debugElementByCss(fixture, URL_INPUT), inputValue);
        tick();
        httpWrite = component.updateModelFromForm();
      }));

      it('with URL', () => {
        expect(httpWrite.url).toBe(inputValue);
      });

      it('with HttpWriteValue', () => {
        expect(httpWrite.writeValues[0].name).toBe(httpWriteValueName);
      });
    });

    describe('returns undefined', () => {
      it('if form values have not been changed', () => {
        httpWriteValueUpdateModelFromFormMock.mockReturnValue(undefined);
        expect(component.updateModelFromForm()).toBe(undefined);
      });
    });
  });

});
