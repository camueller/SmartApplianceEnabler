import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';

@Injectable()
export class LanguageService {

  private subject = new BehaviorSubject('de');

  public getLanguage(): Observable<string> {
    return this.subject;
  }

  public setLanguage(language: string) {
    this.subject.next(language);
  }
}
