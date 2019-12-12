import {TranslateLoader, TranslateModule, TranslateService} from '@ngx-translate/core';
import {FakeTranslateLoader} from '../testing/fake-translate-loader';
import {FormGroup, FormGroupDirective, ReactiveFormsModule} from '@angular/forms';
import {Logger, Options} from '../log/logger';
import {Level} from '../log/level';
import {DebugElement, Type} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';

const translations = require('assets/i18n/de.json');

/** Button events to pass to `DebugElement.triggerEventHandler` for RouterLink event handler */
export const ButtonClickEvents = {
  left:  { button: 0 },
  right: { button: 2 }
};

/** Simulate element click. Defaults to mouse left-button click event. */
export function click(el: DebugElement | HTMLElement, eventObj: any = ButtonClickEvents.left): void {
  if (el instanceof HTMLElement) {
    el.click();
  } else {
    el.triggerEventHandler('click', eventObj);
  }
}

export function debugElementByCss(fixture: ComponentFixture<any>, selector: string): DebugElement {
  return fixture.debugElement.query(By.css(selector));
}

export function createComponentAndConfigure<T>(component: Type<T>): ComponentFixture<T> {
  const fixture = TestBed.createComponent(component);

  const translate = TestBed.get(TranslateService);
  translate.use('de');

  return fixture;
}

export function importsFormsAndTranslate(): any[] {
  return [
    ReactiveFormsModule,
    TranslateModule.forRoot(translateModuleConfig())
  ];
}

export function providers() {
  return [
    Logger,
    {provide: Options, useValue: {level: Level.DEBUG}},
  ];
}

export function translateModuleConfig() {
  return {
    loader: {
      provide: TranslateLoader,
      useValue: new FakeTranslateLoader(translations)
    }
  };
}

export function enterAndCheckInputValue(form: FormGroup, formControlName: string, element: DebugElement, inputValue: string) {
  element.nativeElement.value = inputValue;
  element.nativeElement.dispatchEvent(new Event('input'));
  expect(form.controls[formControlName].value).toBe(inputValue);
}
