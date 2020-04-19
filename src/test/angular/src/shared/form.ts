import {Selector} from 'testcafe';
import {getTranslation} from './ngx-translate';
import {isDebug} from './helper';

const SELECT_OPTION_MAX_KEY_LEN = 50;

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
  const selectorString = `${selectorPrefix || ''} ${selectorBase || ''} ${formControlNamePrefix}[formcontrolname="${formControlName}"] ${formControlNameSuffix || ''}`;
  if (isDebug()) { console.log('Selector: ', selectorString); }
  return Selector(selectorString);
}

export function selectorButton(selectorPrefix?: string, buttonClass?: string) {
  const buttonClassResolved = buttonClass ? `.${buttonClass}` : '';
  const selectorString = `${selectorPrefix || ''} button${buttonClassResolved}`;
  if (isDebug()) { console.log('Selector: ', selectorString); }
  return Selector(selectorString);
}

export async function clickButton(t: TestController, selector: Selector) {
  if (isDebug()) { console.log('Click button ...'); }
  await t.click(selector);
  if (isDebug()) { console.log('... button clicked.'); }
}

export async function inputText(t: TestController, selector: Selector, text: string | undefined): Promise<TestController> {
  if (text) {
    await t.typeText(selector, text);
  } else {
    await t.selectText(selector).pressKey('delete');
  }
  return t;
}

export async function assertInput(t: TestController, selector: Selector, text: string | undefined): Promise<TestController> {
  if (text) {
    await t.expect(selector.value).eql(text);
  }
  return t;
}

export async function setCheckboxEnabled(t: TestController, selector: Selector, enabled: boolean) {
  const checked = await selector.checked;
  if ((enabled && !checked) || (!enabled && checked)) {
    await t.click(selector);
  }
  return t;
}

export async function assertCheckbox(t: TestController, selector: Selector, enabled: boolean) {
  await t.expect(selector.checked).eql(enabled);
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

/**
 * FIXME remove
 * @deprecated Use assertSelectNEW
 */
export async function assertSelect(t: TestController, selector: Selector, regExp: RegExp | undefined): Promise<TestController> {
  if (regExp) {
    await t.expect(selector.value).match(regExp);
  }
  return t;
}

export async function assertSelectNEW(t: TestController, selector: Selector, optionKey: string, i18nPrefix?: string) {
  if (isDebug()) { console.log('optionKey=', optionKey); }
  if (isDebug()) { console.log('i18nPrefix=', i18nPrefix); }
  await t.expect(selector.innerText).eql(getTranslation(optionKey, i18nPrefix));
}

export function getIndexedSelectOptionValueRegExp(value: string) {
  // Angular seems to limit the value length to 50 characters:
  // <option value="de.avanux.smartapplianceenabler.meter.S0Electricit" ...
  return new RegExp(`...${value.substr(0, SELECT_OPTION_MAX_KEY_LEN - 3)}`);
}
