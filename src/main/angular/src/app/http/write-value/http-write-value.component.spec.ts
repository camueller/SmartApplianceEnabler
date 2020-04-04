import {Component, DebugElement, NO_ERRORS_SCHEMA, ViewChild} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {async, ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {
  createComponentAndConfigure,
  debugElementByCss,
  enterAndCheckInputValue,
  importsFormsAndTranslate,
  providers,
  selectOptionValue
} from '../../shared/test-util';
import {HttpWriteValueComponent} from './http-write-value.component';
import {HttpWriteValue} from './http-write-value';
import {By} from '@angular/platform-browser';

@Component({
  template: `
    <app-http-write-value
      [form]="form"
      [httpWriteValue]="httpWriteValue"
      [valueNames]="valueNames"
      [translationPrefix]="translationPrefix"
      [translationKeys]="translationKeys"
      [disableFactorToValue]="disableFactorToValue"
    ></app-http-write-value>`
})
class HttpWriteValueTestHostComponent {
  @ViewChild(HttpWriteValueComponent, {static: true}) testComponent;
  form = new FormGroup({});
  httpWriteValue = new HttpWriteValue();
  valueNames = ['On', 'Off'];
  translationPrefix = 'ControlHttpComponent.';
  translationKeys = ['ControlHttpComponent.On', 'ControlHttpComponent.Off'];
  disableFactorToValue = false;
}

describe('HttpWriteValueComponent', () => {

  const NAME_SELECT = 'select[formcontrolname="name"]';
  const NAME_LABEL = 'label.name';
  const VALUE_INPUT = 'input[formcontrolname="value"]';
  const VALUE_LABEL = 'label.value';
  const FACTORTOVALUE_INPUT = 'input[formcontrolname="factorToValue"]';
  const FACTORTOVALUE_LABEL = 'label.factorToValue';
  const METHOD_SELECT = 'select[formcontrolname="method"]';
  const METHOD_LABEL = 'label.method';

  let component: HttpWriteValueComponent;
  let hostComponent: HttpWriteValueTestHostComponent;
  let fixture: ComponentFixture<HttpWriteValueTestHostComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        HttpWriteValueComponent,
        HttpWriteValueTestHostComponent,
      ],
      imports: importsFormsAndTranslate(),
      providers: providers(),
      schemas: [NO_ERRORS_SCHEMA]
    });
    fixture = createComponentAndConfigure(HttpWriteValueTestHostComponent);
    hostComponent = fixture.componentInstance;
    component = hostComponent.testComponent;

    component.formHandler.markLabelsRequired = jest.fn();

    fixture.detectChanges();
    // fixture.whenStable().then(() => {
    //   console.log('HTML=', fixture.debugElement.nativeElement.innerHTML);
    // });
  }));

  describe('initially', () => {
    it('a HttpWrite exists', () => {
      expect(component.httpWriteValue).toBeTruthy();
    });
  });

  describe('fields', () => {
    describe('name', () => {
      let element: DebugElement;

      beforeEach(() => {
        element = debugElementByCss(fixture, NAME_SELECT);
      });

      it('exists', () => {
        expect(element).toBeTruthy();
      });

      it('has label', () => {
        expect(debugElementByCss(fixture, NAME_LABEL)).toBeTruthy();
      });

      it('has no initial value', () => {
        expect(component.form.controls.name.value).toBeFalsy();
      });

      it('should show options', () => {
        const options = fixture.debugElement.queryAll(By.css(`${NAME_SELECT} option`));
        expect(options[0].nativeElement.text).toBe('Einschalten');
        expect(options[1].nativeElement.text).toBe('Ausschalten');
      });

      it('has the value associated with selected option', () => {
        selectOptionValue(fixture, NAME_SELECT, '1: Off');
        expect(component.form.controls.name.value).toEqual('Off');
        selectOptionValue(fixture, NAME_SELECT, '0: On');
        expect(component.form.controls.name.value).toEqual('On');
      });
    });

    describe('value', () => {
      let element: DebugElement;

      beforeEach(() => {
        element = debugElementByCss(fixture, VALUE_INPUT);
      });

      it('exists', () => {
        expect(element).toBeTruthy();
      });

      it('has label', () => {
        expect(debugElementByCss(fixture, VALUE_LABEL)).toBeTruthy();
      });

      it('can have a value entered', () => {
        enterAndCheckInputValue(component.form, 'value', element, 'on');
      });
    });

    describe('factorToValue', () => {
      let element: DebugElement;

      beforeEach(() => {
        element = debugElementByCss(fixture, FACTORTOVALUE_INPUT);
      });

      describe('enabled (default)', () => {
        it('exists', () => {
          expect(element).toBeTruthy();
        });

        it('has label', () => {
          expect(debugElementByCss(fixture, FACTORTOVALUE_LABEL)).toBeTruthy();
        });

        it('a valid value entered results in a valid form control', () => {
          enterAndCheckInputValue(component.form, 'factorToValue', element, '1.2345');
          expect(component.form.controls.factorToValue.valid).toBeTruthy();
        });

        it('an invalid value entered results in a invalid form control', () => {
          enterAndCheckInputValue(component.form, 'factorToValue', element, 'abc');
          expect(component.form.controls.factorToValue.valid).toBeFalsy();
        });
      });

      describe('disabled', () => {
        beforeEach(() => {
          hostComponent.disableFactorToValue = true;
          fixture.detectChanges();
          element = debugElementByCss(fixture, FACTORTOVALUE_INPUT);
        });

        it('does not exist', () => {
          expect(element).toBeFalsy();
        });

        it('has no label', () => {
          expect(debugElementByCss(fixture, FACTORTOVALUE_LABEL)).toBeFalsy();
        });
      });
    });

    describe('method', () => {
      let element: DebugElement;

      beforeEach(() => {
        element = debugElementByCss(fixture, METHOD_SELECT);
      });

      it('exists', () => {
        expect(element).toBeTruthy();
      });

      it('has label', () => {
        expect(debugElementByCss(fixture, METHOD_LABEL)).toBeTruthy();
      });

      it('has no initial value', () => {
        expect(component.form.controls.name.value).toBeFalsy();
      });

      it('should show options', () => {
        const options = fixture.debugElement.queryAll(By.css(`${METHOD_SELECT} option`));
        expect(options[0].nativeElement.text).toBe('GET');
        expect(options[1].nativeElement.text).toBe('POST');
      });

      it('has the value associated with selected option', () => {
        selectOptionValue(fixture, METHOD_SELECT, '1: POST');
        expect(component.form.controls.method.value).toEqual('POST');
        selectOptionValue(fixture, METHOD_SELECT, '0: GET');
        expect(component.form.controls.method.value).toEqual('GET');
      });
    });
  });

  describe('form', () => {
    it('filling all required inputs results in a valid form', () => {
      expect(component.form.valid).toBeFalsy();
      selectOptionValue(fixture, NAME_SELECT, '0: On');
      expect(component.form.valid).toBeTruthy();
    });
  });

  describe('updateModelFromForm', () => {

    describe('returns HttpWrite', () => {
      const name = '0: On';
      const value = 'on';
      const factorToValue = '1.2345';
      let httpWriteValue: HttpWriteValue;

      beforeEach(fakeAsync(() => {
        selectOptionValue(fixture, NAME_SELECT, name);
        enterAndCheckInputValue(component.form, 'value', debugElementByCss(fixture, VALUE_INPUT), value);
        enterAndCheckInputValue(component.form, 'factorToValue',
          debugElementByCss(fixture, FACTORTOVALUE_INPUT), factorToValue);
        tick();
        selectOptionValue(fixture, METHOD_SELECT, '1: POST');
        httpWriteValue = component.updateModelFromForm();
      }));

      it('with name', () => {
        expect(httpWriteValue.name).toBe('On');
      });

      it('with value', () => {
        expect(httpWriteValue.value).toBe(value);
      });

      it('with factorToValue', () => {
        expect(httpWriteValue.factorToValue).toBe(Number.parseFloat(factorToValue));
      });

      it('with method', () => {
        expect(httpWriteValue.method).toBe('POST');
      });
    });

    describe('returns undefined', () => {
      it('if form values have not been changed', () => {
        expect(component.updateModelFromForm()).toBe(undefined);
      });
    });
  });
});
