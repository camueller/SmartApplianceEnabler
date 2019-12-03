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

  const URL_SELECTOR = 'input[formcontrolname="url"]';

  describe('Bindings', () => {
    let component: HttpWriteTestHostComponent;
    let fixture: ComponentFixture<HttpWriteTestHostComponent>;

    beforeEach(async(() => {
      TestBed.configureTestingModule({
        declarations: [HttpWriteComponent, HttpWriteTestHostComponent],
        imports: importsFormsAndTranslate(),
        providers: providersLoggingAndFormGroupDirective(),
        schemas: [NO_ERRORS_SCHEMA]
      });
      fixture = createComponentAndConfigure(HttpWriteTestHostComponent);
      component = fixture.componentInstance;
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


  describe('Component', () => {
    let component: HttpWriteComponent;
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
      const hostComponent = fixture.componentInstance;
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
        url = fixture.debugElement.query(By.css(URL_SELECTOR));
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
          const error = fixture.debugElement.query(By.css('div.negative'));
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
        const button = fixture.debugElement.query(By.css('i[ng-reflect-ng-class=removeValue0]'));
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
          const url = fixture.debugElement.query(By.css(URL_SELECTOR));
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

});
