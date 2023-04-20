import {FormControl} from '@angular/forms';
import {NotificationType} from './notification-type';

export interface NotificationModel {
  notificationsEnabled: FormControl<boolean>;
  notificationTypes: FormControl<NotificationType[]>;
}
