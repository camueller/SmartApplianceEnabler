import {Selector} from 'testcafe';
import {getTranslation} from './ngx-translate';
import {isDebug} from './helper';

export function selectorInputByFormControlName(formControlName: string, selectorPrefix?: string, selectorBase?: string) {
  return selectorByFormControlName(formControlName, 'input', undefined, selectorPrefix, selectorBase);
}

export function selectorCheckboxByFormControlName(formControlName: string, selectorPrefix?: string, selectorBase?: string) {
  return selectorByFormControlName(formControlName, 'mat-checkbox', undefined, selectorPrefix, selectorBase);
}

export function selectorCheckboxCheckedByFormControlName(formControlName: string, selectorPrefix?: string, selectorBase?: string) {
  return selectorByFormControlName(formControlName, 'mat-checkbox', 'input', selectorPrefix, selectorBase);
}

export function selectorSelectByFormControlName(formControlName: string, selectorPrefix?: string, selectorBase?: string) {
  return selectorByFormControlName(formControlName, 'mat-select', undefined, selectorPrefix, selectorBase);
}

export function selectorSelectedByFormControlName(formControlName: string, selectorPrefix?: string, selectorBase?: string) {
  return selectorByFormControlName(formControlName, 'mat-select', 'span.mat-mdc-select-value-text > span', selectorPrefix, selectorBase);
}

export function selectorStringByFormControlNameOrNgReflectName(formControlName: string, formControlNamePrefix?: string,
                                                               formControlNameSuffix?: string, selectorPrefix?: string,
                                                               selectorBase?: string) {
  const selectorFormControlName = `${selectorPrefix ?? ''} ${selectorBase ?? ''} ${formControlNamePrefix ?? ''}[formcontrolname="${formControlName}"] ${formControlNameSuffix || ''}`;
  const selectorNgReflectName = `${selectorPrefix ?? ''} ${selectorBase ?? ''} ${formControlNamePrefix ?? ''}[ng-reflect-name="${formControlName}"] ${formControlNameSuffix || ''}`;
  return `${selectorFormControlName}, ${selectorNgReflectName}`;
}

export function selectorByFormControlName(formControlName: string, formControlNamePrefix: string, formControlNameSuffix?: string,
                                          selectorPrefix?: string, selectorBase?: string) {
  const selectorString = selectorStringByFormControlNameOrNgReflectName(formControlName, formControlNamePrefix, formControlNameSuffix,
    selectorPrefix, selectorBase);
  if (isDebug()) { console.log('Selector: ', selectorString); }
  return Selector(selectorString);
}

export function selectorButton(selectorPrefix?: string, buttonClass?: string) {
  const buttonClassResolved = buttonClass ? `.${buttonClass}` : '';
  const selectorString = `${selectorPrefix || ''} button${buttonClassResolved}`;
  if (isDebug()) { console.log('Selector: ', selectorString); }
  return selectorString;
}

export async function clickButton(t: TestController, selector: string, options?: any) {
  await clickSelector(t, Selector(selector), 'button')
}

export async function clickSelector(t: TestController, selector: Selector, name: string, options?: any) {
  if (isDebug()) { console.log(`Click ${name} ...`); }
  await t.expect(await selector.exists).ok();
  await t.click(selector, options);
  if (isDebug()) { console.log(`... ${name} clicked.`); }
}

export async function inputText(t: TestController, selector: Selector, text: string | undefined) {
  await t.expect(await Selector(selector).exists).ok();
  if (text) {
    await t.typeText(selector, text);
  } else {
    await t.selectText(selector).pressKey('delete');
  }
}

export async function assertInput(t: TestController, selector: Selector, text: string | undefined) {
  await t.expect(await Selector(selector).exists).ok();
  const actual = await selector.value;
  const expected = text || '';
  await t.expect(actual.toString()).eql(expected.toString());
}

export async function setCheckboxEnabled(t: TestController, selector: Selector, check: boolean) {
  await t.expect(await Selector(selector).exists).ok();
  const checked = await isCheckboxChecked(t, selector);
  if ((check && !checked) || (!check && checked)) {
    await clickSelector(t,  selector, 'checkbox');
  }
}

export async function assertCheckbox(t: TestController, selector: Selector, enabled: boolean) {
  if(enabled) {
    await t.expect(await isCheckboxChecked(t, selector)).ok();
  } else {
    await t.expect(selector.exists).ok();
    await t.expect(await isCheckboxChecked(t, selector)).notOk();
  }
}

function isCheckboxChecked(t: TestController, selector: Selector) {
  return selector.filter('.mdc-checkbox--selected, .mat-mdc-checkbox-checked').exists;
}

export async function selectOption(t: TestController, selector: Selector, value: string) {
  if (isDebug()) { console.log('Open select ...'); }
  await t.expect(await Selector(selector).exists).ok();
  await t.click(selector);
  const optionSelectorString = `mat-option[ng-reflect-value="${value}"]`;
  if (isDebug()) { console.log('Option selector: ', optionSelectorString); }
  const optionSelector = Selector(optionSelectorString);
  const optionSelectorExists = await optionSelector.exists;
  if (isDebug()) { console.log('Option selector exists=', optionSelectorExists); }
  await t.click(optionSelector);
  if (isDebug()) { console.log('clicked'); }
}

export async function assertSelectOption(t: TestController, selector: Selector, optionKey: string, i18nPrefix?: string) {
  if (isDebug()) { console.log(`optionKey=${optionKey} i18nPrefix=${i18nPrefix}`); }
  await t.expect(await Selector(selector).exists).ok();
  if (optionKey) {
    const escapedRegexDe = buildEscaptedRegexString(getTranslation(optionKey, i18nPrefix));
    const escapedRegexEn = buildEscaptedRegexString(getTranslation(optionKey, i18nPrefix, 'en'));
    // Sometimes "innertext" returns english text even though later it is german; due to this time issue we have to accept either language
    await t.expect(selector.innerText).match(new RegExp(`(${escapedRegexDe}|${escapedRegexEn})\s?`)); // innertext may return a string with trailing \t - https://github.com/DevExpress/testcafe/issues/5011
  } else {
    await t.expect(selector.exists).notOk();
  }
}

export async function selectOptionMulti(t: TestController, selector: Selector, values: any[]) {
  await t.click(selector);
  for (const value of values) {
    const selectorString = `mat-option[ng-reflect-value="${value.toString()}"]`;
    if (isDebug()) { console.log('Selector: ', selectorString); }
    await t.expect(await Selector(selectorString).exists).ok();
    await t.click(selectorString);
  }
  await t.pressKey('esc'); // close multi select overlay
}

export async function assertSelectOptionMulti(t: TestController, selector: Selector, values: any[], i18nPrefix?: string) {
  if (values) {
    await t.expect(await Selector(selector).exists).ok();
    const escapedRegexDe = buildEscaptedRegexString(buildSelectOptionsString(values, i18nPrefix))
    const escapedRegexEn = buildEscaptedRegexString(buildSelectOptionsString(values, i18nPrefix, 'en'));
    const actualSelectedOptionsString = await selector.innerText;
    await t.expect(actualSelectedOptionsString.trim()).match(new RegExp(`(${escapedRegexDe}|${escapedRegexEn})\s?`)); // innertext may return a string with trailing \t - https://github.com/DevExpress/testcafe/issues/5011
  }
}

function buildSelectOptionsString(values: any[], i18nPrefix?: string, lang?: string) {
  const selectOptionStrings = [];
  values?.forEach(value => {
    selectOptionStrings.push(getTranslation(value, i18nPrefix, lang));
  });
  return selectOptionStrings.join(', ');
}

function buildEscaptedRegexString(input: string) {
  return input.replace(/[.*+?^${}()|[\]\\]/g, '.'); // replace regex syntax characters with "."
}

