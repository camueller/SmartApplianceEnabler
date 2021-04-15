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

export enum NotificationType {
  CONTROL_ON = 'CONTROL_ON',
  CONTROL_OFF = 'CONTROL_OFF',
  EVCHARGER_VEHICLE_NOT_CONNECTED = 'EVCHARGER_VEHICLE_NOT_CONNECTED',
  EVCHARGER_VEHICLE_CONNECTED = 'EVCHARGER_VEHICLE_CONNECTED',
  EVCHARGER_CHARGING = 'EVCHARGER_CHARGING',
  EVCHARGER_CHARGING_COMPLETED = 'EVCHARGER_CHARGING_COMPLETED',
  EVCHARGER_ERROR = 'EVCHARGER_ERROR',
  COMMUNICATION_ERROR = 'COMMUNICATION_ERROR',
}
