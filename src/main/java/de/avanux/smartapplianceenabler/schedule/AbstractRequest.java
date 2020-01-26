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

package de.avanux.smartapplianceenabler.schedule;

import de.avanux.smartapplianceenabler.appliance.ActiveIntervalChangedListener;
import de.avanux.smartapplianceenabler.control.Control;
import de.avanux.smartapplianceenabler.meter.Meter;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class AbstractRequest {
    private transient Logger logger = LoggerFactory.getLogger(AbstractRequest.class);
    private transient String applianceId;
    private transient Vector<RequestState> stateHistory = new Vector<>();
    private transient LocalDateTime stateChangedAt;
    private transient List<RequestStateChangedListener> requestStateChangedListeners = new ArrayList<>();
    private transient Meter meter;
    private transient Control control;


    public AbstractRequest() {
        initStateHistory();
    }

    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    public void setMeter(Meter meter) {
        this.meter = meter;
    }

    public void setControl(Control control) {
        this.control = control;
    }

    public void addRequestStateChangedListener(RequestStateChangedListener listener) {
        this.requestStateChangedListeners.add(listener);
    }

    private void initStateHistory() {
        this.stateHistory.clear();
        stateHistory.add(RequestState.CREATED);
    }

    public void stateTransitionTo(LocalDateTime now, RequestState state) {
        this.stateHistory.add(state);
        this.stateChangedAt = now;
        requestStateChangedListeners.forEach(listener -> {
            logger.debug("{}: Notifying {} {}", applianceId, ActiveIntervalChangedListener.class.getSimpleName(),
                    listener.getClass().getSimpleName());

        });
    }

    public RequestState getState() {
        return stateHistory.lastElement();
    }

    public boolean wasInState(RequestState state) {
        return stateHistory.contains(state);
    }

}
