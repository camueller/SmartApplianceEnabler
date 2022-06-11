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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class SocScriptExecutionResult {
    private DecimalFormat percentageFormat;
    public Double soc;
    public Boolean pluggedIn;
    public String plugInTime;
    Pair<Double, Double> location;

    public SocScriptExecutionResult() {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
        percentageFormat = (DecimalFormat) nf;
        percentageFormat.applyPattern("#'%'");
    }

    @Override
    public String toString() {
        return "SocScriptExecutionResult{" +
                "soc=" + soc != null ? percentageFormat.format(soc) : "null" +
                ", pluggedIn=" + pluggedIn +
                ", pluggedInTime='" + plugInTime + '\'' +
                ", location=" + location +
                '}';
    }
}
