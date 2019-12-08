import {async, ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {Component, DebugElement, NO_ERRORS_SCHEMA, ViewChild} from '@angular/core';
import {HttpWriteComponent} from './http-write.component';
import {ControlValueName} from '../control/control-value-name';
import {By} from '@angular/platform-browser';
import {
  click,
  createComponentAndConfigure,
  importsFormsAndTranslate,
  providersLoggingAndFormGroupDirective
} from '../shared/test-util';
import {HttpWriteValue} from '../http-write-value/http-write-value';
import {FormGroup} from '@angular/forms';
import {HttpWrite} from './http-write';

const httpWriteValueUpdateModelFromFormMock = jest.fn();

@Component({
  template: `<app-http-write
    [form]="form"
    [httpWrite]="httpWrite"
    [translationKeys]="['dummy']"
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

@Component({selector: 'app-http-write-value', template: ''})
class HttpWriteValueStubComponent {
  updateModelFromForm(): HttpWriteValue | undefined {
    return httpWriteValueUpdateModelFromFormMock();
  }
}

describe('HttpWriteComponent', () => {

  const URL_INPUT_SELECTOR = 'input[formcontrolname="url"]';
  const ERROR_ELEMENT_SELECTOR = 'div.negative';
  const REMOVE_HTTPWRITE_BUTTON_SELECTOR = 'i[ng-reflect-ng-class=removeValue0]';

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
      providers: providersLoggingAndFormGroupDirective(),
      schemas: [NO_ERRORS_SCHEMA]
    });
    fixture = createComponentAndConfigure(HttpWriteTestHostComponent);
    hostComponent = fixture.componentInstance;
    component = hostComponent.testComponent;

    component.formHandler.markLabelsRequired = jest.fn();
    component.translationKeys = ['ControlHttpComponent.On', 'ControlHttpComponent.Off'];

    fixture.detectChanges();

    component.httpWrite.writeValues[0].name = ControlValueName.On;
    component.translationPrefix = 'ControlHttpComponent.';

    fixture.detectChanges();
    // fixture.whenStable().then(() => {
    //   console.log('HTML=', fixture.debugElement.nativeElement.innerHTML);
    // });
  }));

  describe('Bindings', () => {
    it('should handle "remove" event', () => {
      fixture.detectChanges();
      fixture.whenStable().then(() => {
        // console.log('HTML=', fixture.debugElement.nativeElement.innerHTML);
        const removeButton = fixture.debugElement.query(By.css('.buttonRemoveHttpWrite'));
        removeButton.nativeElement.click();
        expect(hostComponent.onHttpWriteRemoveCalled).toBeTruthy();
      });
    });
  });


  describe('Initially', () => {
    it('a HttpWrite exists', () => {
      expect(component.httpWrite).toBeTruthy();
    });

    it('the HttpWrite contains one HttpWriteValue', async(() => {
      expect(component.httpWrite.writeValues.length).toBe(1);
    }));
  });

  describe('URL field', () => {
    let url: DebugElement;
    let urlNE: any;

    beforeEach(() => {
      url = fixture.debugElement.query(By.css(URL_INPUT_SELECTOR));
      urlNE = url.nativeElement;
    });

    it('exists', () => {
      expect(url).toBeTruthy();
    });

    it('has label', () => {
      expect(fixture.debugElement.query(By.css('label.url'))).toBeTruthy();
    });

    it('with valid URL should make the form valid', () => {
      const inputValue = 'http://web.de';
      urlNE.value = inputValue;
      urlNE.dispatchEvent(new Event('input'));
      expect(component.form.controls.url.value).toBe(inputValue);
      expect(component.form.valid).toBeTruthy();
    });

    it('with invalid URL should make the form invalid', () => {
      const inputValue = 'http:web.de';
      urlNE.value = inputValue;
      urlNE.dispatchEvent(new Event('input'));
      expect(component.form.controls.url.value).toBe(inputValue);
      expect(component.form.valid).toBeFalsy();
    });

    it('with invalid URL should display an error message', () => {
      const inputValue = 'http:web.de';
      urlNE.value = inputValue;
      urlNE.dispatchEvent(new Event('input'));
      fixture.detectChanges();
      fixture.whenStable().then(() => {
        const error = fixture.debugElement.query(By.css(ERROR_ELEMENT_SELECTOR));
        expect(error.nativeElement.innerHTML).toBe('Die URL muss gültig sein');
      });
    });
  });

  describe('Button "Weiterer Zustand/Aktion"', () => {
    beforeEach(() => {
      expect(component.httpWrite.writeValues.length).toBe(1);
      expect(component.form.dirty).toBeFalsy();
      const button = fixture.debugElement.query(By.css('button.addValue'));
      click(button);
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

  describe('Button "X" (Remove HttpWriteValue)', () => {
    beforeEach(() => {
      expect(component.form.dirty).toBeFalsy();
      const button = fixture.debugElement.query(By.css(REMOVE_HTTPWRITE_BUTTON_SELECTOR));
      click(button);
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

  describe('updateModelFromForm', () => {

    describe('returns HttpWrite', () => {
      const inputValue = 'http://web.de';
      const httpWriteValueName = 'httpWriteValue.name';
      let httpWrite;

      beforeEach(fakeAsync(() => {
        httpWriteValueUpdateModelFromFormMock.mockReturnValue({name: httpWriteValueName} as HttpWriteValue);
        const url = fixture.debugElement.query(By.css(URL_INPUT_SELECTOR));
        const urlNE = url.nativeElement;
        urlNE.value = inputValue;
        urlNE.dispatchEvent(new Event('input'));
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
