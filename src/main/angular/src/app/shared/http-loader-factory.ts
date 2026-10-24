import {TranslateHttpLoader} from '@ngx-translate/http-loader';
import {HttpClient} from '@angular/common/http';

export function HttpLoaderFactory(_http: HttpClient) {
  return new TranslateHttpLoader();
}
