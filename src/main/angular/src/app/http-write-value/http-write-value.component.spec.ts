import {Component, DebugElement, NO_ERRORS_SCHEMA, ViewChild} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {createComponentAndConfigure, debugElementByCss, importsFormsAndTranslate, providers} from '../shared/test-util';
import {HttpWriteValueComponent} from './http-write-value.component';

@Component({
  template: `<app-http-write-value
    [form]="form"
    [translationKeys]="['dummy']"
  ></app-http-write-value>`
})
class HttpWriteValueTestHostComponent {
  @ViewChild(HttpWriteValueComponent, { static: true }) testComponent;
  form = new FormGroup({});
}

describe('HttpWriteValueComponent', () => {

  const VALUE_INPUT = 'input[formcontrolname="value"]';
  const VALUE_LABEL = 'label.url';

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
    // component.translationKeys = ['ControlHttpComponent.On', 'ControlHttpComponent.Off'];

    fixture.detectChanges();

    // component.httpWrite.writeValues[0].name = ControlValueName.On;
    // component.translationPrefix = 'ControlHttpComponent.';

    fixture.detectChanges();
    fixture.whenStable().then(() => {
      console.log('HTML=', fixture.debugElement.nativeElement.innerHTML);
    });
  }));

  describe('Initially', () => {
    it('a HttpWrite exists', () => {
      expect(component.httpWriteValue).toBeTruthy();
    });
  });

  describe('value field', () => {
    let value: DebugElement;

    beforeEach(() => {
      value = debugElementByCss(fixture, VALUE_INPUT);
    });

    it('exists', () => {
      expect(value).toBeTruthy();
    });

    it('has label', () => {
      expect(debugElementByCss(fixture, VALUE_LABEL)).toBeTruthy();
    });

    it('with valid value should make the form valid', () => {
      const inputValue = 'on';
      value.nativeElement.value = inputValue;
      value.nativeElement.dispatchEvent(new Event('input'));
      expect(component.form.controls.value.value).toBe(inputValue);
      expect(component.form.valid).toBeTruthy();
    });

  });

});
