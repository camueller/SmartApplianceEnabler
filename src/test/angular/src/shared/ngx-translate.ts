const de = require('../../../../main/angular/src/assets/i18n/de.json');
const en = require('../../../../main/angular/src/assets/i18n/en.json');

export function getTranslation(key: string, prefix?: string, lang = 'de') {
  let config = de;
  if(lang === 'en') {
    config = en;
  }
  config['F-00000001-000000000001-00'] = 'Vitocal 300';
  const resolvedKey = prefix ? `${prefix}${key}` : key;
  const tranlation = config[resolvedKey];
  return tranlation || key;
}
