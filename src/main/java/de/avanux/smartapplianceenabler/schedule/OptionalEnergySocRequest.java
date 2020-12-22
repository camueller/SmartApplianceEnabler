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

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class OptionalEnergySocRequest extends SocRequest {

    public OptionalEnergySocRequest(Integer evId) {
        setEnabled(true);
        setEvId(evId);
        setSoc(100);
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        // there should always only be one instance of this request
        // therefore multiple instances should considered to be equal
        // this behaviour is also required to be able to remove itself as listener successfully
        return true;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 47)
                .toHashCode();
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
