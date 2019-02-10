import {TranslateLoader} from '@ngx-translate/core';
import {Observable, of} from 'rxjs';

export class FakeTranslateLoader implements TranslateLoader {

   constructor(public translations: any) {}

  getTranslation(lang: string): Observable<any> {
    return of(this.translations);
  }
}
