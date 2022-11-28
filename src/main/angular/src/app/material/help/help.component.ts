import {Component, Input} from '@angular/core';
import {LanguageService} from '../../shared/language-service';

@Component({
  selector: 'app-help',
  templateUrl: './help.component.html',
  styleUrls: ['./help.component.scss']
})
export class HelpComponent {
  @Input()
  helpfile: string;
  @Input()
  anchor: string;
  @Input()
  cssClass = 'middle';
  private baseUrl = 'https://github.com/camueller/SmartApplianceEnabler/blob/master/doc/';
  private language = 'DE';

  constructor(private languageService: LanguageService) {
    this.languageService.getLanguage().subscribe(language => {
      this.language = language.toUpperCase();
    });
  }

  public get url() {
    const url = `${this.baseUrl}${this.helpfile}_${this.language}.md`;
    return this.anchor ? `${url}#user-content-${this.anchor}` : url;
  }

  public get iconclass() {
    return `HelpComponent__icon--${this.cssClass}`;
  }
}
