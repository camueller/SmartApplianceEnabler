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
  return selectorByFormControlName(formControlName, 'mat-select', 'span.mat-select-value-text > span', selectorPrefix, selectorBase);
}

export function selectorByFormControlName(formControlName: string, formControlNamePrefix: string, formControlNameSuffix?: string,
                                          selectorPrefix?: string, selectorBase?: string) {
  const selectorFormControlName = `${selectorPrefix || ''} ${selectorBase || ''} ${formControlNamePrefix}[formcontrolname="${formControlName}"] ${formControlNameSuffix || ''}`;
  const selectorNgReflectName = `${selectorPrefix || ''} ${selectorBase || ''} ${formControlNamePrefix}[ng-reflect-name="${formControlName}"] ${formControlNameSuffix || ''}`;
  const selectorString = `${selectorFormControlName}, ${selectorNgReflectName}`;
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
  if (isDebug()) { console.log(`Click button ${selector} ...`); }
  await t.click(selector, options);
  if (isDebug()) { console.log('... button clicked.'); }
}

export async function inputText(t: TestController, selector: Selector, text: string | undefined) {
  if (text) {
    await t.typeText(selector, text);
  } else {
    await t.selectText(selector).pressKey('delete');
  }
}

export async function assertInput(t: TestController, selector: Selector, text: string | undefined) {
  const actual = await selector.value;
  const expected = text || '';
  await t.expect(actual.toString()).eql(expected.toString());
}

export async function setCheckboxEnabled(t: TestController, selector: Selector, enabled: boolean) {
  const checked = JSON.parse(await isCheckboxChecked(t, selector.find('input')));
  if ((enabled && !checked) || (!enabled && checked)) {
    await t.click(selector);
  }
}

export async function assertCheckbox(t: TestController, selector: Selector, enabled: boolean) {
  // await t.expect(selector.withAttribute('aria-checked', enabled ? 'true' : 'false').exists).ok();
  await t.expect(await isCheckboxChecked(t, selector)).eql(enabled ? 'true' : 'false');
}

async function isCheckboxChecked(t: TestController, selector: Selector) {
  return await selector.getAttribute('aria-checked');
}

export async function selectOptionByAttribute(t: TestController, selector: Selector, value: string) {
  if (isDebug()) { console.log('Open select ...'); }
  await t.click(selector);
  const optionSelectorString = `mat-option[ng-reflect-value="${value}"]`;
  if (isDebug()) { console.log('Option selector: ', optionSelectorString); }
  const optionSelector = Selector(optionSelectorString);
  const optionSelectorExists = await optionSelector.exists;
  if (isDebug()) { console.log('Option selector exists=', optionSelectorExists); }
  await t.click(optionSelector);
  if (isDebug()) { console.log('clicked'); }
}

export async function assertSelect(t: TestController, selector: Selector, optionKey: string, i18nPrefix?: string) {
  if (isDebug()) { console.log('optionKey=', optionKey); }
  if (isDebug()) { console.log('i18nPrefix=', i18nPrefix); }
  if (optionKey) {
    await t.expect(selector.innerText).eql(getTranslation(optionKey, i18nPrefix));
  } else {
    await t.expect(selector.exists).notOk();
  }
}
