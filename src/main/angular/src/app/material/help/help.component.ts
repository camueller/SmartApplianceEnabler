import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-help',
  templateUrl: './help.component.html',
  styleUrls: ['./help.component.scss']
})
export class HelpComponent {

  @Input()
  helpfile: string;
  @Input()
  language = 'DE';
  @Input()
  anchor: string;
  @Input()
  cssClass = 'middle';
  private baseUrl = 'https://github.com/camueller/SmartApplianceEnabler/blob/2.0/doc/';

  public get url() {
    const url = `${this.baseUrl}${this.helpfile}_${this.language}.md`;
    return this.anchor ? `${url}#user-content-${this.anchor}` : url;
  }

  public get iconclass() {
    return `HelpComponent__icon--${this.cssClass}`;
  }
}
