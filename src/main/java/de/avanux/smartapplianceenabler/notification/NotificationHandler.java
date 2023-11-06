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

package de.avanux.smartapplianceenabler.notification;

import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import de.avanux.smartapplianceenabler.appliance.ApplianceManager;
import de.avanux.smartapplianceenabler.semp.webservice.DeviceInfo;
import de.avanux.smartapplianceenabler.semp.webservice.Identification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class NotificationHandler implements ApplianceIdConsumer {
    private transient Logger logger = LoggerFactory.getLogger(NotificationHandler.class);

    private String applianceId;
    private String command;
    private String senderId;
    private int maxCommunicationErrors = 10;
    private Notifications requestedNotifications = null;
    private int errorCountPerDay = 0;
    private LocalDate errorDate;
    private boolean communicationErrorNotificationSentToday;

    public NotificationHandler(String applianceId, String command, String senderId, Integer maxCommunicationErrors) {
        this.applianceId = applianceId;
        this.command = command;
        this.senderId = senderId;
        if(maxCommunicationErrors != null) {
            this.maxCommunicationErrors = maxCommunicationErrors;
        }
        logger.debug("{}: command={} senderId={} maxCommunicationErrors={}",
                applianceId, command, senderId, this.maxCommunicationErrors);
    }

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    public void setRequestedNotifications(Notifications requestedNotifications) {
        this.requestedNotifications = requestedNotifications;
        if(requestedNotifications != null) {
            if(requestedNotifications.getTypes() != null) {
                logger.debug("{}: enabled notifications {}", applianceId, requestedNotifications.getTypes());
            }
            else {
                logger.debug("{}: all notifications enabled", applianceId);
            }
        }
    }

    protected boolean isRequestedNotification(NotificationType type) {
        return this.requestedNotifications != null
                && (this.requestedNotifications.getTypes() == null
                || this.requestedNotifications.getTypes().contains(type.name()));
    }

    private boolean isErrorNotification(NotificationType type) {
        return type.name().contains("ERROR");
    }

    private boolean shouldSendErrorNotification() {
        if(errorCountPerDay == 0) {
            errorDate = LocalDateTime.now().toLocalDate();
        }
        else if(!errorDate.equals(LocalDateTime.now().toLocalDate())) {
            errorCountPerDay = 0;
            communicationErrorNotificationSentToday = false;
        }
        errorCountPerDay++;
        return errorCountPerDay > maxCommunicationErrors && !communicationErrorNotificationSentToday;
    }

    public void sendNotification(NotificationType type) {
        if(isRequestedNotification(type)) {
            logger.debug("{}: Checking notification preconditions: errorCountPerDay={}", applianceId, errorCountPerDay);
            if(!isErrorNotification(type) || shouldSendErrorNotification()) {
                String message = getMessage(type);
                if(type == NotificationType.COMMUNICATION_ERROR) {
                    message = Messages.getString(NotificationType.COMMUNICATION_ERROR.name(), maxCommunicationErrors);
                }
                try {
                    logger.debug("{}: Executing notification command: {}", applianceId, command);
                    DeviceInfo deviceInfo = ApplianceManager.getInstance().getDeviceInfo(applianceId);
                    Identification identification = deviceInfo.getIdentification();
                    ProcessBuilder builder = new ProcessBuilder(
                            command,
                            senderId != null ? senderId : applianceId, // $1
                            identification.getDeviceName(), // $2
                            identification.getDeviceType(), // $3
                            identification.getDeviceVendor(), // $4
                            identification.getDeviceSerial(), // $5
                            type.name(), // $6
                            message // $7
                    );
                    Process p = builder.start();
                    int rc = p.waitFor();
                    logger.debug("{}: Notification command exited with return code {}", applianceId, rc);
                    if(type == NotificationType.COMMUNICATION_ERROR) {
                        communicationErrorNotificationSentToday = true;
                    }
                } catch (Exception e) {
                    logger.error("{}: Error executing notification command {}", applianceId, command, e);
                }
            } else {
                logger.debug("{}: Ignoring notification: errorNotification={} shouldSendErrorNotification={}",
                        applianceId, isErrorNotification(type), shouldSendErrorNotification());
            }
        }
        else {
            logger.debug("{}: Ignoring notification of type {}", applianceId, type);
        }
    }

    protected String getMessage(NotificationType type) {
        return Messages.getString(type.name());
    }
}

class Messages {
    private static final String BUNDLE_NAME = "messages";
    private static Locale locale = null;
    private static ResourceBundle resourceBundle = null;

    private Messages() {
    }

    private static ResourceBundle getResourceBundle() {
        if(resourceBundle == null) {
            if("de".equals(System.getProperty("user.language"))) {
                locale = new Locale("de");
            } else {
                locale = new Locale("en");
            }
            resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
        }
        return resourceBundle;
    }

    public static String getString(String key) {
        try {
            return getResourceBundle().getString(key);
        } catch (MissingResourceException e) {
            return buildErrorMessage(key);
        }
    }

    public static String getString(String key, Object... params) {
        try {
            return MessageFormat.format(getResourceBundle().getString(key), params);
        } catch (MissingResourceException e) {
            return buildErrorMessage(key);
        }
    }

    private static String buildErrorMessage(String key) {
        return "Key " + key + " not found in resource bundle " + BUNDLE_NAME + " for locale " + locale;
    }
}
