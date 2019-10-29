/*
 * Copyright (C) 2019 Axel Müller <axel.mueller@avanux.de>
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

package de.avanux.smartapplianceenabler.http;

import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import de.avanux.smartapplianceenabler.protocol.ContentProtocolHandler;
import de.avanux.smartapplianceenabler.util.ParentWithChild;
import de.avanux.smartapplianceenabler.util.RegexUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpHandler implements ApplianceIdConsumer {

    private transient Logger logger = LoggerFactory.getLogger(HttpHandler.class);
    private transient String applianceId;
    private transient HttpTransactionExecutor httpTransactionExecutor;

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    public void setHttpTransactionExecutor(HttpTransactionExecutor httpTransactionExecutor) {
        this.httpTransactionExecutor = httpTransactionExecutor;
    }

    public float getFloatValue(ParentWithChild<HttpRead, HttpReadValue> read,
                               ContentProtocolHandler contentProtocolHandler) {
        String protocolHandlerValue = getValue(read, contentProtocolHandler);
        if(protocolHandlerValue != null) {
            String valueExtractionRegex = read.child().getExtractionRegex();
            String extractedValue = RegexUtil.getMatchingGroup1(protocolHandlerValue, valueExtractionRegex);
            String parsableString = extractedValue.replace(',', '.');
            Float value;
            Double factorToValue = read.child().getFactorToValue();
            if(factorToValue != null) {
                value = Double.valueOf(Double.parseDouble(parsableString) * factorToValue).floatValue();
            }
            else {
                value = Double.valueOf(Double.parseDouble(parsableString)).floatValue();
            }
            logger.debug("{}: value={} protocolHandlerValue={} valueExtractionRegex={} extractedValue={}",
                    applianceId, value, protocolHandlerValue, valueExtractionRegex, extractedValue);
            return value;
        }
        return 0.0f;
    }

    public boolean getBooleanValue(ParentWithChild<HttpRead, HttpReadValue> read,
                                   ContentProtocolHandler contentProtocolHandler) {
        String protocolHandlerValue = getValue(read, contentProtocolHandler);
        if(protocolHandlerValue != null) {
            String valueExtractionRegex = read.child().getExtractionRegex();
            boolean match = RegexUtil.isMatch(protocolHandlerValue, valueExtractionRegex);
            logger.debug("{}: match={} protocolHandlerValue={} valueExtractionRegex={}",
                    applianceId, match, protocolHandlerValue, valueExtractionRegex);
            return match;
        }
        return false;
    }

    private String getValue(ParentWithChild<HttpRead, HttpReadValue> read,
                               ContentProtocolHandler contentProtocolHandler) {
        if(read != null) {
            String url = read.parent().getUrl();
            String data = read.child().getData();
            HttpMethod httpMethod = data != null ? HttpMethod.POST : HttpMethod.GET;
            String path = read.child().getPath();
            String response = this.httpTransactionExecutor.execute(httpMethod, url, data);
            logger.debug("{}: url={} httpMethod={} data={} path={}",
                    applianceId, url, httpMethod, data, path);
            if(response != null) {
                logger.debug("{}: Response: {}", applianceId, response);
                String protocolHandlerValue = response;
                if(contentProtocolHandler != null) {
                    contentProtocolHandler.parse(response);
                    protocolHandlerValue = contentProtocolHandler.readValue(path);
                }
                return protocolHandlerValue;
            }
        }
        return null;
    }
}
