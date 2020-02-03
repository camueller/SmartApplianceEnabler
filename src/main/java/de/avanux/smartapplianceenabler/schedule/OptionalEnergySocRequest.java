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

import de.avanux.smartapplianceenabler.control.ev.ElectricVehicleCharger;
import org.joda.time.LocalDateTime;

public class OptionalEnergySocRequest extends SocRequest {

    public OptionalEnergySocRequest(Integer evId) {
        setEnabled(true);
        setEvId(evId);
        setSoc(100);
    }

    @Override
    public boolean isUsingOptionalEnergy() {
        return true;
    }

    @Override
    public Integer getMin(LocalDateTime now) {
        return 0;
    }

    public void onTimeframeIntervalStateChanged(LocalDateTime now, TimeframeIntervalState previousState,
                                                TimeframeIntervalState newState) {
        super.onTimeframeIntervalStateChanged(now, previousState, newState);
        if(newState == TimeframeIntervalState.ACTIVE) {
            ((ElectricVehicleCharger) getControl()).retrieveSoc(now);
        }
    }

    @Override
    public String toString() {
        String text = super.toString();
        text += "/";
        text += "Optional Energy";
        return text;
    }
}
