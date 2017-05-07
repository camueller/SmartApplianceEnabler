/*
 * Copyright (C) 2017 Axel Müller <axel.mueller@avanux.de>
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

package de.avanux.smartapplianceenabler.log;

import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 * This logger adds the appliance id to each log statement.
 */
public class ApplianceLogger implements Logger, ApplianceIdConsumer {

    private Logger logger;
    private String applianceId;

    public ApplianceLogger(Logger logger) {
        this.logger = logger;
    }

    public static ApplianceLogger createForAppliance(Logger logger, String applianceId) {
        ApplianceLogger applianceLogger = new ApplianceLogger(logger);
        applianceLogger.setApplianceId(applianceId);
        return applianceLogger;
    }

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    private String withApplianceId(String text) {
        return applianceId + ": " + text;
    }

    public String getName() {
        return logger.getName();
    }

    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        logger.trace(marker, withApplianceId(format), arg1, arg2);
    }

    public void trace(Marker marker, String format, Object... argArray) {
        logger.trace(marker, withApplianceId(format), argArray);
    }

    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    public void trace(String format, Object... arguments) {
        logger.trace(withApplianceId(format), arguments);
    }

    public void warn(Marker marker, String format, Object arg) {
        logger.warn(marker, withApplianceId(format), arg);
    }

    public void error(Marker marker, String format, Object arg1, Object arg2) {
        logger.error(marker, withApplianceId(format), arg1, arg2);
    }

    public void error(Marker marker, String msg) {
        logger.error(marker, msg);
    }

    public void error(Marker marker, String msg, Throwable t) {
        logger.error(marker, withApplianceId(msg), t);
    }

    public void error(String format, Object... arguments) {
        logger.error(withApplianceId(format), arguments);
    }

    public void info(Marker marker, String msg) {
        logger.info(marker, withApplianceId(msg));
    }

    public void info(Marker marker, String format, Object arg1, Object arg2) {
        logger.info(marker, withApplianceId(format), arg1, arg2);
    }

    public void error(String msg) {
        logger.error(withApplianceId(msg));
    }

    public void trace(Marker marker, String msg, Throwable t) {
        logger.trace(marker, withApplianceId(msg), t);
    }

    public void error(String format, Object arg) {
        logger.error(withApplianceId(format), arg);
    }

    public void warn(String msg, Throwable t) {
        logger.warn(withApplianceId(msg), t);
    }

    public void debug(Marker marker, String msg) {
        logger.debug(marker, withApplianceId(msg));
    }

    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        logger.debug(marker, withApplianceId(format), arg1, arg2);
    }

    public void info(String msg, Throwable t) {
        logger.info(withApplianceId(msg), t);
    }

    public void warn(Marker marker, String format, Object... arguments) {
        logger.warn(marker, withApplianceId(format), arguments);
    }

    public void debug(String format, Object... arguments) {
        logger.debug(withApplianceId(format), arguments);
    }

    public void debug(Marker marker, String msg, Throwable t) {
        logger.debug(marker, withApplianceId(msg), t);
    }

    public void info(String format, Object... arguments) {
        logger.info(withApplianceId(format), arguments);
    }

    public void trace(String format, Object arg) {
        logger.trace(withApplianceId(format), arg);
    }

    public void debug(String format, Object arg1, Object arg2) {
        logger.debug(withApplianceId(format), arg1, arg2);
    }

    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    public void warn(String format, Object arg1, Object arg2) {
        logger.warn(withApplianceId(format), arg1, arg2);
    }

    public boolean isTraceEnabled(Marker marker) {
        return logger.isTraceEnabled(marker);
    }

    public void debug(String format, Object arg) {
        logger.debug(withApplianceId(format), arg);
    }

    public void error(Marker marker, String format, Object arg) {
        logger.error(marker, withApplianceId(format), arg);
    }

    public void warn(Marker marker, String msg) {
        logger.warn(marker, withApplianceId(msg));
    }

    public void info(Marker marker, String msg, Throwable t) {
        logger.info(marker, withApplianceId(msg), t);
    }

    public void trace(String msg, Throwable t) {
        logger.trace(withApplianceId(msg), t);
    }

    public boolean isWarnEnabled(Marker marker) {
        return logger.isWarnEnabled(marker);
    }

    public void error(Marker marker, String format, Object... arguments) {
        logger.error(marker, withApplianceId(format), arguments);
    }

    public void warn(String format, Object arg) {
        logger.warn(withApplianceId(format), arg);
    }

    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    public void trace(String msg) {
        logger.trace(withApplianceId(msg));
    }

    public void trace(Marker marker, String msg) {
        logger.trace(marker, withApplianceId(msg));
    }

    public void error(String msg, Throwable t) {
        logger.error(withApplianceId(msg), t);
    }

    public void info(String msg) {
        logger.info(withApplianceId(msg));
    }

    public void warn(String msg) {
        logger.warn(withApplianceId(msg));
    }

    public void error(String format, Object arg1, Object arg2) {
        logger.error(withApplianceId(format), arg1, arg2);
    }

    public void trace(String format, Object arg1, Object arg2) {
        logger.trace(withApplianceId(format), arg1, arg2);
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        logger.warn(marker, withApplianceId(format), arg1, arg2);
    }

    public void info(String format, Object arg) {
        logger.info(withApplianceId(format), arg);
    }

    public boolean isInfoEnabled(Marker marker) {
        return logger.isInfoEnabled(marker);
    }

    public void info(Marker marker, String format, Object... arguments) {
        logger.info(marker, format, arguments);
    }

    public void warn(Marker marker, String msg, Throwable t) {
        logger.warn(marker, withApplianceId(msg), t);
    }

    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    public void debug(String msg, Throwable t) {
        logger.debug(withApplianceId(msg), t);
    }

    public void debug(String msg) {
        logger.debug(withApplianceId(msg));
    }

    public void trace(Marker marker, String format, Object arg) {
        logger.trace(marker, withApplianceId(format), arg);
    }

    public boolean isErrorEnabled(Marker marker) {
        return logger.isErrorEnabled(marker);
    }

    public void debug(Marker marker, String format, Object arg) {
        logger.debug(marker, withApplianceId(format), arg);
    }

    public void info(Marker marker, String format, Object arg) {
        logger.info(marker, withApplianceId(format), arg);
    }

    public boolean isDebugEnabled(Marker marker) {
        return logger.isDebugEnabled(marker);
    }

    public void info(String format, Object arg1, Object arg2) {
        logger.info(withApplianceId(format), arg1, arg2);
    }

    public void warn(String format, Object... arguments) {
        logger.warn(withApplianceId(format), arguments);
    }

    public void debug(Marker marker, String format, Object... arguments) {
        logger.debug(marker, withApplianceId(format), arguments);
    }

}
