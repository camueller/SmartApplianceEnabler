/*
 * Copyright (C) 2018 Axel Müller <axel.mueller@avanux.de>
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

package de.avanux.smartapplianceenabler.schedule;

import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import de.avanux.smartapplianceenabler.appliance.TimeframeIntervalChangedListener;
import de.avanux.smartapplianceenabler.meter.Meter;

import java.io.Serializable;
import java.time.LocalDateTime;

public interface Request extends Serializable, ApplianceIdConsumer, TimeframeIntervalChangedListener {

    void disconnectMqttClient();

    void setMeter(Meter meter);

    boolean isUsingOptionalEnergy(LocalDateTime now);

    void setTimeframeIntervalStateProvider(TimeframeIntervalStateProvider timeframeIntervalStateProvider);

    Integer getMin(LocalDateTime now);

    Integer getMax(LocalDateTime now);

    Integer getMinOrMax(LocalDateTime now);

    boolean isNext();

    void setNext(boolean next);

    boolean isEnabled();

    boolean isEnabledBefore();

    void setEnabled(boolean enabled);

    Boolean isAcceptControlRecommendations();

    void setAcceptControlRecommendations(Boolean acceptControlRecommendations);

    boolean isFinished(LocalDateTime now);

    boolean isControlOn();

    Integer getRuntime(LocalDateTime now);

    LocalDateTime getControlStatusChangedAt();

    void init();

    void update();

    void remove();

    String toString(LocalDateTime now);
}
