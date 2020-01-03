import { Selector } from 'testcafe'; // first import testcafe selectors

fixture('Testcafe')
  .page('http://devexpress.github.io/testcafe/example');

test('My first test', async t => {
  const nameField = Selector('#developer-name');
  const submitButton = Selector('#submit-button');
  await t.typeText(nameField, 'John Smith').click(submitButton);
});
