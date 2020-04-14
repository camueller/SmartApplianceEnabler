import {Component, DebugElement, NO_ERRORS_SCHEMA, ViewChild} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {
  createComponentAndConfigure,
  debugElementByCss,
  defaultImports,
  defaultProviders,
  enterAndCheckInputValue
} from '../../shared/test-util';
import {HttpWriteValueComponent} from './http-write-value.component';
import {HttpWriteValue} from './http-write-value';
import {HarnessLoader} from '@angular/cdk/testing';
import {TestbedHarnessEnvironment} from '@angular/cdk/testing/testbed';
import {MatSelectHarness} from '@angular/material/select/testing';
import {MatFormFieldHarness} from '@angular/material/form-field/testing';
import {MatInputHarness} from '@angular/material/input/testing';

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

  const NAME_FORM_FIELD = '.sae__value-name';
  const NAME_SELECT = '[formcontrolname="name"]';
  const VALUE_FORM_FIELD = '.HttpWriteValueComponent__value';
  const VALUE_INPUT = '[formcontrolname="value"]';
  const FACTORTOVALUE_FORM_FIELD = '.sae__factorToValue';
  const FACTORTOVALUE_INPUT = '[formcontrolname="factorToValue"]';
  const METHOD_FORM_FIELD = '.sae__http-method';
  const METHOD_SELECT = '[formcontrolname="method"]';

  let component: HttpWriteValueComponent;
  let hostComponent: HttpWriteValueTestHostComponent;
  let fixture: ComponentFixture<HttpWriteValueTestHostComponent>;
  let harnessLoader: HarnessLoader;

  let nameFormField: MatFormFieldHarness;
  let nameSelect: MatSelectHarness;
  let valueFormField: MatFormFieldHarness;
  let valueInput: MatInputHarness;
  let factorToValueFormField: MatFormFieldHarness;
  let factorToValueInput: MatInputHarness;
  let methodFormField: MatFormFieldHarness;
  let methodSelect: MatSelectHarness;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      declarations: [
        HttpWriteValueComponent,
        HttpWriteValueTestHostComponent,
      ],
      imports: defaultImports(),
      providers: defaultProviders(),
      schemas: [NO_ERRORS_SCHEMA]
    });
    fixture = createComponentAndConfigure(HttpWriteValueTestHostComponent);
    harnessLoader = TestbedHarnessEnvironment.loader(fixture);
    hostComponent = fixture.componentInstance;
    component = hostComponent.testComponent;

    nameFormField = await harnessLoader.getHarness(MatFormFieldHarness.with({selector: NAME_FORM_FIELD}));
    nameSelect = await harnessLoader.getHarness(MatSelectHarness.with({selector: NAME_SELECT}));
    valueFormField = await harnessLoader.getHarness(MatFormFieldHarness.with({selector: VALUE_FORM_FIELD}));
    valueInput = await harnessLoader.getHarness(MatInputHarness.with({selector: VALUE_INPUT}));
    factorToValueFormField = await harnessLoader.getHarness(MatFormFieldHarness.with({selector: FACTORTOVALUE_FORM_FIELD}));
    factorToValueInput = await harnessLoader.getHarness(MatInputHarness.with({selector: FACTORTOVALUE_INPUT}));
    methodFormField = await harnessLoader.getHarness(MatFormFieldHarness.with({selector: METHOD_FORM_FIELD}));
    methodSelect = await harnessLoader.getHarness(MatSelectHarness.with({selector: METHOD_SELECT}));

    fixture.detectChanges();
    // fixture.whenStable().then(() => {
    //   console.log('HTML=', fixture.debugElement.nativeElement.innerHTML);
    // });
  });

  describe('initially', () => {
    it('a HttpWrite exists', () => {
      expect(component.httpWriteValue).toBeTruthy();
    });
  });

  describe('fields', () => {
    describe('name', () => {

      beforeEach(async () => {
        await nameSelect.open();
      });

      it('exists', () => {
        expect(nameSelect).toBeDefined();
      });

      it('has label', async () => {
        expect(await nameFormField.getLabel()).toBe('Zustand / Aktion *');
      });

      it('has no initial value', () => {
        expect(component.form.controls.name.value).toBeFalsy();
      });

      it('should show options', async () => {
        const options = await nameSelect.getOptions();
        expect(await options[0].getText()).toBe('Einschalten');
        expect(await options[1].getText()).toBe('Ausschalten');
      });

      it('has the value associated with selected option "Einschalten"', async () => {
        await nameSelect.clickOptions({text: 'Einschalten'});
        expect(component.form.controls.name.value).toEqual('On');
      });

      it('has the value associated with selected option "Ausschalten"', async () => {
        await nameSelect.clickOptions({text: 'Ausschalten'});
        expect(component.form.controls.name.value).toEqual('Off');
      });
    });

    describe('value', () => {
      it('exists', () => {
        expect(valueInput).toBeDefined();
      });

      it('has label', async () => {
        expect(await valueFormField.getLabel()).toBe('Wert / Daten');
      });

      it('can have a value entered', async () => {
        enterAndCheckInputValue(valueInput, 'on');
      });
    });

    describe('factorToValue', () => {
      describe('enabled (default)', () => {
        it('exists', () => {
          expect(factorToValueInput).toBeDefined();
        });

        it('has label', async () => {
          expect(await factorToValueFormField.getLabel()).toBe('Umrechnungsfaktor');
        });

        it('a valid value entered results in a valid form control', async () => {
          enterAndCheckInputValue(factorToValueInput, '1.2345');
          expect(await factorToValueFormField.hasErrors()).toBe(false);
        });

        it('an invalid value entered results in a invalid form control', async () => {
          enterAndCheckInputValue(factorToValueInput, 'abc');
          await factorToValueInput.blur();
          expect(await factorToValueFormField.getTextErrors()).toStrictEqual(['Der Faktor muss eine (Gleitkomma-) Zahl sein']);
        });
      });

      describe('disabled', () => {
        let element: DebugElement;

        beforeEach(() => {
          hostComponent.disableFactorToValue = true;
          fixture.detectChanges();
          element = debugElementByCss(fixture, FACTORTOVALUE_INPUT);
        });

        it('does not exist', () => {
          expect(element).toBeFalsy();
        });
      });
    });

    describe('method', () => {

      beforeEach(async () => {
        await methodSelect.open();
      });

      it('exists', () => {
        expect(methodSelect).toBeTruthy();
      });

      it('has label', async () => {
        expect(await methodFormField.getLabel()).toBe('Methode');
      });

      it('has no initial value', () => {
        expect(component.form.controls.name.value).toBeFalsy();
      });

      it('should show options', async () => {
        const options = await methodSelect.getOptions();
        expect(await options[0].getText()).toBe('GET');
        expect(await options[1].getText()).toBe('POST');
      });

      it('has the value associated with selected option "GET"', async () => {
        await methodSelect.clickOptions({text: 'GET'});
        expect(component.form.controls.method.value).toEqual('GET');
      });

      it('has the value associated with selected option "POST"', async () => {
        await methodSelect.clickOptions({text: 'POST'});
        expect(component.form.controls.method.value).toEqual('POST');
      });
    });
  });

  describe('form', () => {
    it('filling all required inputs results in a valid form', async () => {
      expect(component.form.valid).toBeFalsy();
      await nameSelect.open();
      await nameSelect.clickOptions({text: 'Einschalten'});
      expect(component.form.valid).toBeTruthy();
    });
  });

  describe('updateModelFromForm', () => {

    describe('returns HttpWrite', () => {
      const name = '0: On';
      const value = 'on';
      const factorToValue = '1.2345';
      let httpWriteValue: HttpWriteValue;

      beforeEach(async () => {
        await nameSelect.open();
        await nameSelect.clickOptions({text: 'Einschalten'});
        enterAndCheckInputValue(valueInput, 'on');
        enterAndCheckInputValue(factorToValueInput, '1.2345');
        await methodSelect.open();
        await methodSelect.clickOptions({text: 'POST'});
        httpWriteValue = component.updateModelFromForm();
      });

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
