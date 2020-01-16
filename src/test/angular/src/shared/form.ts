import { Selector } from 'testcafe';

const SELECT_OPTION_MAX_KEY_LEN = 50;

export function selectorInputByFormControlName(formcontrolname: string, selectorPrefix?: string, selectorBase?: string) {
  return selectorByFormControlName(formcontrolname, 'input', selectorPrefix, selectorBase);
}

export function selectorSelectByFormControlName(formcontrolname: string, selectorPrefix?: string, selectorBase?: string) {
  return selectorByFormControlName(formcontrolname, 'select', selectorPrefix, selectorBase);
}

export function selectorByFormControlName(formControlName: string, formcontrolType: string,
                                          selectorPrefix?: string, selectorBase?: string) {
  return Selector(`${selectorPrefix || ''} ${selectorBase || ''} ${formcontrolType}[formcontrolname="${formControlName}"]`);
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

export async function assertSelect(t: TestController, selector: Selector, regExp: RegExp | undefined): Promise<TestController> {
  if (regExp) {
    await t.expect(selector.value).match(regExp);
  }
  return t;
}

export async function assertCheckbox(t: TestController, selector: Selector, enabled: boolean | undefined): Promise<TestController> {
  if (enabled) {
    await t.expect(selector.checked).eql(enabled);
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

export async function selectOptionByAttribute(t: TestController, selector: Selector, value: string, numberPrefix?: boolean) {
  // Angular seems to limit the value length to 50 characters:
  // <option value="de.avanux.smartapplianceenabler.meter.S0Electricit" ...
  let valueOrPattern: string | RegExp = value.substr(0, SELECT_OPTION_MAX_KEY_LEN);
  if (numberPrefix) {
    // Some options have an index prefix:
    // <option value="0: de.avanux.smartapplianceenabler.meter.S0Electricit" ...
    valueOrPattern = this.getIndexedSelectOptionValueRegExp(value);
  }
  await t
    .click(selector)
    .click(selector.find('option').withAttribute('value', valueOrPattern));
  return t;
}

export function getIndexedSelectOptionValueRegExp(value: string) {
  // Angular seems to limit the value length to 50 characters:
  // <option value="de.avanux.smartapplianceenabler.meter.S0Electricit" ...
  return new RegExp(`...${value.substr(0, SELECT_OPTION_MAX_KEY_LEN - 3)}`);
}
