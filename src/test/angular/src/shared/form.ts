export async function inputText(t: TestController, selector: Selector, text: string | undefined): Promise<TestController> {
  if (text) {
    await t.typeText(selector, text);
  } else {
    await t.selectText(selector).pressKey('delete');
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

export async function selectOptionByAttribute(t: TestController, selector: Selector, value: string) {
  // Angular seems to limit the value length to 50 characters along with an index prefix:
  // <option value="0: de.avanux.smartapplianceenabler.meter.S0Electri" ...
  // therefore we ignore the first 3 characters and using the remaining 47 characters when looking for a match
  const pattern = `...${value.substr(0, 47)}`;
  await t
    .click(selector)
    .click(selector.find('option').withAttribute('value', new RegExp(pattern)));
  return t;
}
