import {TranslateLoader, TranslateModule, TranslateService} from '@ngx-translate/core';
import {FakeTranslateLoader} from '../testing/fake-translate-loader';
import {FormGroup, ReactiveFormsModule} from '@angular/forms';
import {Logger, Options} from '../log/logger';
import {Level} from '../log/level';
import {DebugElement, Type} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {MaterialModule} from '../material/material.module';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {MatInputHarness} from '@angular/material/input/testing';

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

export function selectOptionValue(fixture: ComponentFixture<any>, selector: string, value: string) {
  const nativeElement = fixture.debugElement.query(By.css(selector)).nativeElement;
  nativeElement.value = value;
  nativeElement.dispatchEvent(new Event('change'));
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

export function defaultImports(): any[] {
  return [
    ReactiveFormsModule,
    MaterialModule,
    NoopAnimationsModule,
    TranslateModule.forRoot(translateModuleConfig())
  ];
}

export function defaultProviders() {
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

export async function enterAndCheckInputValue(valueInput: MatInputHarness, value: string) {
  await valueInput.setValue(value);
  expect(await valueInput.getValue()).toBe(value);
}
