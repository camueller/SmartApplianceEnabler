import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {Component, NO_ERRORS_SCHEMA, ViewChild} from '@angular/core';
import {HttpWriteComponent} from './http-write.component';
import {ControlValueName} from '../../control/control-value-name';
import {
  createComponentAndConfigure,
  debugElementByCss,
  defaultImports,
  defaultProviders,
  enterAndCheckInputValue
} from '../../shared/test-util';
import {FormGroup} from '@angular/forms';
import {HttpWrite} from './http-write';
import {HttpWriteValue} from '../write-value/http-write-value';
import {MatFormFieldHarness} from '@angular/material/form-field/testing';
import {MatInputHarness} from '@angular/material/input/testing';
import {TestbedHarnessEnvironment} from '@angular/cdk/testing/testbed';
import {HarnessLoader} from '@angular/cdk/testing';
import {MatButtonHarness} from '@angular/material/button/testing';

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
  translationKeys = [];

  onHttpWriteRemove() {
    this.onHttpWriteRemoveCalled = true;
  }
}

describe('HttpWriteComponent', () => {

  const URL_FORM_FIELD = '.sae__url';
  const URL_INPUT = '[formcontrolname="url"]';
  const ADD_HTTPWRITEVALUE_BUTTON = 'button.addHttpWriteValue';
  const REMOVE_HTTPWRITEVALUE_BUTTON = 'button.removeHttpWriteValue';
  const REMOVE_HTTPWRITE_BUTTON = 'button.removeHttpWrite';

  let component: HttpWriteComponent;
  let hostComponent: HttpWriteTestHostComponent;
  let fixture: ComponentFixture<HttpWriteTestHostComponent>;
  let harnessLoader: HarnessLoader;

  let urlFormField: MatFormFieldHarness;
  let urlInput: MatInputHarness;
  let addHttpWriteValueButton: MatButtonHarness;
  let removeHttpWriteValueButton: MatButtonHarness;
  let removeHttpWriteButton: MatButtonHarness;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      declarations: [
        HttpWriteComponent,
        HttpWriteTestHostComponent,
        HttpWriteValueStubComponent,
      ],
      imports: defaultImports(),
      providers: defaultProviders(),
      schemas: [NO_ERRORS_SCHEMA]
    });
    fixture = createComponentAndConfigure(HttpWriteTestHostComponent);
    harnessLoader = TestbedHarnessEnvironment.loader(fixture);
    hostComponent = fixture.componentInstance;
    component = hostComponent.testComponent;

    urlFormField = await harnessLoader.getHarness(MatFormFieldHarness.with({selector: URL_FORM_FIELD}));
    urlInput = await harnessLoader.getHarness(MatInputHarness.with({selector: URL_INPUT}));
    addHttpWriteValueButton = await harnessLoader.getHarness(MatButtonHarness.with({selector: ADD_HTTPWRITEVALUE_BUTTON}));
    removeHttpWriteValueButton = await harnessLoader.getHarness(MatButtonHarness.with({selector: REMOVE_HTTPWRITEVALUE_BUTTON}));
    removeHttpWriteButton = await harnessLoader.getHarness(MatButtonHarness.with({selector: REMOVE_HTTPWRITE_BUTTON}));

    component.translationKeys = ['ControlHttpComponent.On', 'ControlHttpComponent.Off'];

    fixture.detectChanges();

    component.httpWrite.writeValues[0].name = ControlValueName.On;
    component.translationPrefix = 'ControlHttpComponent.';

    fixture.detectChanges();
    // fixture.whenStable().then(() => {
    //   console.log('HTML=', fixture.debugElement.nativeElement.innerHTML);
    // });
  });

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

      it('exists', () => {
        expect(urlInput).toBeDefined();
      });

      it('has label', async () => {
        expect(await urlFormField.getLabel()).toBe('URL *');
      });

      it('a valid value entered results in a valid form control', async () => {
        enterAndCheckInputValue(urlInput, 'http://web.de');
        expect(await urlFormField.hasErrors()).toBe(false);
      });

      it('an invalid value entered results in a invalid form control', async () => {
        enterAndCheckInputValue(urlInput, 'http:web.de');
        await urlInput.blur();
        expect(await urlFormField.hasErrors()).toBe(true);
      });

      it('an invalid URL should display an error message', async () => {
        enterAndCheckInputValue(urlInput, 'http:web.de');
        await urlInput.blur();
        expect(await urlFormField.getTextErrors()).toStrictEqual(['Die URL muss gültig sein']);
      });
    });
  });

  describe('buttons', () => {
    describe('Weiterer Zustand/Aktion', () => {

      beforeEach(() => {
        expect(component.httpWrite.writeValues.length).toBe(1);
        expect(component.form.dirty).toBeFalsy();
      });

      it('exists', async () => {
        expect(await addHttpWriteValueButton).toBeDefined();
      });

      it('has label', async () => {
        expect(await addHttpWriteValueButton.getText()).toBe('Weiterer Zustand/Aktion');
      });

      it('should add a HttpWriteValue', async () => {
        await addHttpWriteValueButton.click();
        expect(component.httpWrite.writeValues.length).toBe(2);
      });

      it('set form to dirty', async () => {
        await addHttpWriteValueButton.click();
        expect(component.form.dirty).toBeTruthy();
      });
    });

    describe('Zustand löschen', () => {

      beforeEach(() => {
        expect(component.form.dirty).toBeFalsy();
      });

      it('exists', async () => {
        expect(await removeHttpWriteValueButton).toBeDefined();
      });

      it('has label', async () => {
        expect(await removeHttpWriteValueButton.getText()).toBe('Zustand löschen');
      });

      it('should remove a HttpWriteValue', async () => {
        await removeHttpWriteValueButton.click();
        expect(component.httpWrite.writeValues.length).toBe(0);
      });

      it('set form to dirty', async () => {
        await removeHttpWriteValueButton.click();
        expect(component.form.dirty).toBeTruthy();
      });
    });

    describe('URL löschen', () => {

      beforeEach(() => {
        expect(component.form.dirty).toBeFalsy();
      });

      it('exists', async () => {
        expect(await removeHttpWriteButton).toBeDefined();
      });

      it('has label', async () => {
        expect(await removeHttpWriteButton.getText()).toBe('URL löschen');
      });

      it('should remove HttpWrite', async () => {
        await removeHttpWriteButton.click();
        expect(hostComponent.onHttpWriteRemoveCalled).toBe(true);
      });

      it('set form to dirty', () => {
        fixture.whenStable().then(() => {
          expect(component.form.dirty).toBeTruthy();
        });
      });
    });
  });

  describe('form', () => {
    it('filling all required inputs results in a valid form', async () => {
      expect(component.form.valid).toBeFalsy();
      await enterAndCheckInputValue(urlInput, 'http://web.de');
      expect(component.form.valid).toBeTruthy();
    });
  });

  describe('updateModelFromForm', () => {

    describe('returns HttpWrite', () => {
      const inputValue = 'http://web.de';
      const httpWriteValueName = 'httpWriteValue.name';
      let httpWrite: HttpWrite;

      beforeEach(async () => {
        httpWriteValueUpdateModelFromFormMock.mockReturnValue({name: httpWriteValueName} as HttpWriteValue);
        await enterAndCheckInputValue(urlInput, inputValue);
        httpWrite = component.updateModelFromForm();
      });

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
