/*
 * Copyright (C) 2020 Axel Müller <axel.mueller@avanux.de>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

import {
  assertCheckbox,
  assertSelectOptionMulti,
  selectOptionMulti,
  selectorCheckboxByFormControlName,
  selectorCheckboxCheckedByFormControlName,
  selectorSelectByFormControlName,
  setCheckboxEnabled
} from '../../shared/form';
import {Notifications} from '../../../../../main/angular/src/app/notification/notifications';
import {NotificationType} from '../../../../../main/angular/src/app/notification/notification-type';

export class NotificationPage {

  public static async setNotifications(t: TestController, notifications: Notifications) {
    await NotificationPage.setNotificationEnabled(t, !! notifications);
    if (notifications?.types) {
      await NotificationPage.setNotificationTypes(t, notifications.types);
    }
  }
  public static async assertNotifications(t: TestController, notifications: Notifications) {
    await NotificationPage.assertNotificationEnabled(t, !! notifications);
    if (notifications?.types) {
      await NotificationPage.assertNotificationTypes(t, notifications.types);
    }
  }

  public static async setNotificationEnabled(t: TestController, enabled: boolean) {
    await setCheckboxEnabled(t, selectorCheckboxByFormControlName('notificationsEnabled'), enabled);
  }
  public static async assertNotificationEnabled(t: TestController, enabled: boolean) {
    await assertCheckbox(t, selectorCheckboxCheckedByFormControlName('notificationsEnabled'), enabled);
  }

  public static async setNotificationTypes(t: TestController, notificationTypes: NotificationType[]) {
    await selectOptionMulti(t, selectorSelectByFormControlName('notificationTypes'), notificationTypes);
  }

  public static async assertNotificationTypes(t: TestController, notificationTypes: NotificationType[]) {
    await assertSelectOptionMulti(t, selectorSelectByFormControlName('notificationTypes'), notificationTypes,
      'NotificationComponent.type.');
  }
}
