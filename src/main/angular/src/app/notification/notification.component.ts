import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {Logger} from '../log/logger';
import {FormControl, FormGroup, FormGroupDirective} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {ListItem} from '../shared/list-item';
import {Notifications} from './notifications';
import {NotificationModel} from './notification.model';
import {isRequired} from '../shared/form-util';

@Component({
  selector: 'app-notification',
  templateUrl: './notification.component.html',
  styleUrls: ['./notification.component.scss'],
})
export class NotificationComponent implements OnChanges {
  @Input()
  notifications: Notifications;
  @Input()
  types: string[];
  @Input()
  configured: boolean;
  form: FormGroup<NotificationModel>;
  typeListItems: ListItem[] = [];

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private translate: TranslateService
  ) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.form = this.parent.form;
    if (changes.notifications) {
      if (changes.notifications.currentValue) {
        this.notifications = changes.notifications.currentValue;
      }
      if (changes.types.currentValue) {
        this.types = changes.types.currentValue;
        this.buildTypeList();
      }
      this.expandParentForm();
    }
  }

  public get typesPlaceholder() {
    return this.isEnabled() ? 'NotificationComponent.type.placeholder' : undefined;
  }

  isEnabled() {
    return this.form.controls.notificationsEnabled?.value;
  }

  setEnabled(enabled: boolean) {
    if (enabled) {
      this.form.controls.notificationTypes.enable();
      if (!this.notifications) {
        this.notifications = new Notifications();
      }
    } else {
      this.notifications = undefined;
      this.form.controls.notificationTypes.disable();
    }
  }

  buildTypeList() {
    const typeKeys = this.types.map(key => `NotificationComponent.type.${key}`);
    this.translate.get(typeKeys).subscribe(translatedStrings => {
      this.typeListItems = [];
      Object.keys(translatedStrings).forEach(key => {
        this.typeListItems.push({value: key.split('.')[2], viewValue: translatedStrings[key]});
      });
    });
  }

  isRequired(formControlName: string) {
    return isRequired(this.form, formControlName);
  }

  expandParentForm() {
    this.form.addControl('notificationsEnabled', new FormControl({value: !!this.notifications, disabled: !this.configured}));
    this.form.addControl('notificationTypes', new FormControl(this.notifications?.types));
    this.setEnabled(this.isEnabled());
    this.form.controls.notificationsEnabled.valueChanges.subscribe(value => {
      this.setEnabled(value);
      this.form.markAsDirty();
    });
  }

  updateModelFromForm(): Notifications | undefined {
    const notificationsEnabled = this.form.controls.notificationsEnabled.value;
    const notificationTypes = this.form.controls.notificationTypes.value;

    if (!notificationsEnabled) {
      return undefined;
    }

    this.notifications.types = notificationTypes;
    return this.notifications;
  }
}
