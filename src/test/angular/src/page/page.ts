export function baseUrl() {
  const url = process.env.E2E_TEST_URL;
  if (!url) {
    throw new Error('E2E_TEST_URL has to be defined!');
  }
  return url;
}
