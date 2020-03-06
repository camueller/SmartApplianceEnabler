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

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OptionalEnergySocRequest extends SocRequest {

    public OptionalEnergySocRequest(Integer evId) {
        setEnabled(true);
        setEvId(evId);
        setSoc(100);
    }

    public OptionalEnergySocRequest(Integer evId, Integer energy) {
        setEvId(evId);
        setEnergy(energy);
    }

    protected Logger getLogger() {
        return LoggerFactory.getLogger(OptionalEnergySocRequest.class);
    }

    @Override
    public boolean isUsingOptionalEnergy() {
        return true;
    }

    @Override
    public Boolean isAcceptControlRecommendations() {
        return true;
    }

    @Override
    public Integer getMin(LocalDateTime now) {
        return 0;
    }

    @Override
    public String toString() {
        return toString(LocalDateTime.now());
    }

    @Override
    public String toString(LocalDateTime now) {
        String text = super.toString(now);
        text += "/";
        text += "Optional Energy";
        return text;
    }
}
