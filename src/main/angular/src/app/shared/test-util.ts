import {TranslateLoader, TranslateModule, TranslateService} from '@ngx-translate/core';
import {FakeTranslateLoader} from '../testing/fake-translate-loader';
import {FormGroup, FormGroupDirective, ReactiveFormsModule} from '@angular/forms';
import {Logger, Options} from '../log/logger';
import {Level} from '../log/level';
import {Type} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';

const translations = require('assets/i18n/de.json');

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

export function providersLoggingAndFormGroupDirective() {
  return [
    Logger,
    {provide: Options, useValue: {level: Level.DEBUG}},
    {provide: FormGroupDirective, useValue: {form: new FormGroup({})}}
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
