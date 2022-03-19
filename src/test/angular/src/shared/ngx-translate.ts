const config = require('../../../../main/angular/src/assets/i18n/de.json');

export function getTranslation(key: string, prefix?: string) {
  config['F-00000001-000000000001-00'] = 'Vitocal 300';
  const resolvedKey = prefix ? `${prefix}${key}` : key;
  const tranlation = config[resolvedKey];
  return tranlation || key;
}
