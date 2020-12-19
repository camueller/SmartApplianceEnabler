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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class NotificationHandler implements ApplianceIdConsumer {
    public static final String CONFIGURATION_KEY_NOTIFICATION_COMMAND = "Notification.Commmand";

    private transient Logger logger = LoggerFactory.getLogger(NotificationHandler.class);

    private String applianceId;
    private String command;
    private String senderId;
    private Set<String> requestedNotifications = null;

    public NotificationHandler(String applianceId, String command, String senderId) {
        this.applianceId = applianceId;
        this.command = command;
        this.senderId = senderId;
        logger.debug("{}: command={} senderId={}", applianceId, command, senderId);
    }

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    public void addRequestedNotifications(Notifications requestedNotifications) {
        if(requestedNotifications != null) {
            if(this.requestedNotifications == null) {
                this.requestedNotifications = new HashSet<>();
            }
            List<String> keys = requestedNotifications.getTypes();
            if(keys != null) {
                this.requestedNotifications.addAll(keys);
                logger.debug("{}: enabled notifications {}", applianceId, requestedNotifications.getTypes());
            }
            else {
                logger.debug("{}: all notifications enabled", applianceId);
            }
        }
    }

    protected boolean isRequestedNotification(NotificationType type) {
        return this.requestedNotifications != null
                && (this.requestedNotifications.size() == 0 || this.requestedNotifications.contains(type.name()));
    }

    public void sendNotification(NotificationType type) {
        if(isRequestedNotification(type)) {
            ResourceBundle messages = ResourceBundle.getBundle("messages", new Locale("de", "DE"));
            String message = messages.getString(type.name());
            try {
                logger.debug("{}: Executing notification command: {}", applianceId, command);
                ProcessBuilder builder = new ProcessBuilder(
                        command,
                        senderId != null ? senderId : applianceId,
                        type.name(),
                        message);
                Process p = builder.start();
                int rc = p.waitFor();
                logger.debug("{}: Notification command exited with return code {}", applianceId, rc);
            } catch (Exception e) {
                logger.error("{}: Error executing notification command {}", applianceId, command, e);
            }
        }
    }
}
