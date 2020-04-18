import { Selector } from 'testcafe';
import {getTranslation} from './ngx-translate';

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
  return Selector(`${selectorPrefix || ''} ${selectorBase || ''} ${formControlNamePrefix}[formcontrolname="${formControlName}"] ${formControlNameSuffix || ''}`);
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

/**
 * FIXME number prefix kann weg
 */
export async function selectOptionByAttribute(t: TestController, selector: Selector, value: string, numberPrefix?: boolean) {
  // Angular seems to limit the value length to 50 characters:
  // <option value="de.avanux.smartapplianceenabler.meter.S0Electricit" ...
  let valueOrPattern: string | RegExp = value.substr(0, SELECT_OPTION_MAX_KEY_LEN);
  if (numberPrefix) {
    // Some options have an index prefix:
    // <option value="0: de.avanux.smartapplianceenabler.meter.S0Electricit" ...
    valueOrPattern = this.getIndexedSelectOptionValueRegExp(value);
  }
  await t.click(selector);
  await t.click(Selector(`mat-option[ng-reflect-value="${valueOrPattern}"]`));
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

export async function assertSelectNEW(t: TestController, selector: Selector, optionKey: string, optionKeyPrefix?: string) {
  console.log('optionKey=', optionKey);
  console.log('optionKeyPrefix=', optionKeyPrefix);
  await t.expect(selector.innerText).eql(getTranslation(optionKey, optionKeyPrefix));
}

export function getIndexedSelectOptionValueRegExp(value: string) {
  // Angular seems to limit the value length to 50 characters:
  // <option value="de.avanux.smartapplianceenabler.meter.S0Electricit" ...
  return new RegExp(`...${value.substr(0, SELECT_OPTION_MAX_KEY_LEN - 3)}`);
}
