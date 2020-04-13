import 'jest-preset-angular';
import './jestGlobalMocks'; // browser mocks globally available for every test

/**
 * Avoid Testing: TypeError: getComputedStyle(...).getPropertyValue is not a function
 * https://github.com/telerik/kendo-angular/issues/1505
 */
Object.defineProperty(window, 'getComputedStyle', {
  value: () => ({
    getPropertyValue: (prop) => {
      return '';
    }
  })
});
