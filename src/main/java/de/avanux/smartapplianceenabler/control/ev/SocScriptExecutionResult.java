/*
 * Copyright (C) 2022 Axel Müller <axel.mueller@avanux.de>
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

package de.avanux.smartapplianceenabler.control.ev;

import org.apache.commons.lang3.tuple.Pair;

public class SocScriptExecutionResult {
    public Integer soc;
    public Boolean pluggedIn;
    public String pluginTime;
    Pair<Double, Double> location;

    public SocScriptExecutionResult() {
    }

    public SocScriptExecutionResult(Integer soc, Boolean pluggedIn, String pluginTime, Pair<Double, Double> location) {
        this.soc = soc;
        this.pluggedIn = pluggedIn;
        this.pluginTime = pluginTime;
        this.location = location;
    }

    @Override
    public String toString() {
        return "SocScriptExecutionResult{" +
                "soc=" + soc +
                ", pluggedIn=" + pluggedIn +
                ", pluginTime=" + pluginTime +
                ", location=" + location +
                '}';
    }
}
