/*
 * Copyright (C) 2023 Axel Müller <axel.mueller@avanux.de>
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

package de.avanux.smartapplianceenabler.util;

import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import de.avanux.smartapplianceenabler.http.HttpHandler;
import de.avanux.smartapplianceenabler.protocol.ContentProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class ValueExtractor implements ApplianceIdConsumer {

    private transient Logger logger = LoggerFactory.getLogger(ValueExtractor.class);
    private transient DecimalFormat doubleFormat;

    private transient String applianceId;

    public ValueExtractor() {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
        doubleFormat = (DecimalFormat) nf;
        doubleFormat.applyPattern("#.#####");
    }

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    public Double getDoubleValue(String inputValue,
                                 String valueExtractionRegex, Double factorToValue,
                                 Double defaultValue) {
        if(Environment.isHttpDisabled()) {
            return defaultValue;
        }
        if(inputValue != null) {
            String extractedValue = null;
            if(valueExtractionRegex != null) {
                extractedValue = RegexUtil.getMatchingGroup1(inputValue, valueExtractionRegex);
            }
            String parsableString = (extractedValue != null ? extractedValue : inputValue)
                    .replace(',', '.');
            Double value;
            if(factorToValue != null) {
                value = Double.parseDouble(parsableString) * factorToValue;
            }
            else {
                value = Double.parseDouble(parsableString);
            }
            logger.debug("{}: value={} inputValue={} valueExtractionRegex={} extractedValue={} factorToValue={}",
                    applianceId, doubleFormat.format(value), inputValue, valueExtractionRegex, extractedValue, factorToValue);
            return value;
        }
        return null;
    }

    public boolean getBooleanValue(String inputValue,
                                   String valueExtractionRegex,
                                   boolean defaultValue) {
        if(Environment.isHttpDisabled()) {
            return defaultValue;
        }
        if(inputValue != null) {
            boolean match = RegexUtil.isMatch(inputValue, valueExtractionRegex);
            logger.debug("{}: match={} inputValue={} valueExtractionRegex={}",
                    applianceId, match, inputValue, valueExtractionRegex);
            return match;
        }
        return false;
    }
}
