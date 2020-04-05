import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {SettingsService} from '../../settings/settings-service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {

  @Output()
  public sidenavToggle = new EventEmitter();
  version: string;

  constructor(
    private settingsService: SettingsService
  ) { }

  ngOnInit() {
    this.settingsService.getInfo().subscribe(info => this.version = info.version);
  }

  public onToggleSidenav = () => {
    this.sidenavToggle.emit();
  }
}
