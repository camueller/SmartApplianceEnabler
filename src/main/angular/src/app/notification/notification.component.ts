import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {FormHandler} from '../shared/form-handler';
import {Logger} from '../log/logger';
import {FormGroup, FormGroupDirective} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {ListItem} from '../shared/list-item';
import {Notifications} from './notifications';

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
  enabled: boolean;
  form: FormGroup;
  typeListItems: ListItem[] = [];
  formHandler: FormHandler;

  constructor(private logger: Logger,
              private parent: FormGroupDirective,
              private translate: TranslateService
  ) {
    this.formHandler = new FormHandler();
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
    return this.form.controls.enabled?.value;
  }

  setEnabled(enabled: boolean) {
    if (enabled) {
      this.form.controls.types.enable();
      if (!this.notifications) {
        this.notifications = new Notifications();
      }
    } else {
      this.notifications = undefined;
      this.form.controls.types.disable();
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

  expandParentForm() {
    this.formHandler.addFormControl(this.form, 'enabled', {value: !!this.notifications, disabled: !this.configured});
    this.formHandler.addFormControl(this.form, 'types', this.notifications?.types);
    this.setEnabled(this.isEnabled());
    this.form.controls.enabled.valueChanges.subscribe(value => {
      this.setEnabled(value);
      this.form.markAsDirty();
    });
  }

  updateModelFromForm(): Notifications | undefined {
    const enabled = this.form.controls.enabled.value;
    const types = this.form.controls.types.value;

    if (!enabled) {
      return undefined;
    }

    this.notifications.types = types;
    return this.notifications;
  }
}
