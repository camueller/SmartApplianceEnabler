export async function setCheckboxEnabled(t: TestController, selector: Selector, enabled: boolean) {
  const checked = await selector.checked;
  if ((enabled && !checked) || (!enabled && checked)) {
    await t.click(selector);
  }
  return t;
}
