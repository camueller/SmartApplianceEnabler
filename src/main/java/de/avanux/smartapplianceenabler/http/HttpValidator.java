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

import de.avanux.smartapplianceenabler.control.ev.EVWriteValueName;
import de.avanux.smartapplianceenabler.util.ParentWithChild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

public class HttpValidator {

    private transient Logger logger = LoggerFactory.getLogger(HttpValidator.class);
    private transient String applianceId;

    public HttpValidator(String applianceId) {
        this.applianceId = applianceId;
    }

    public boolean validateReads(Collection<String> valueNames, List<HttpRead> httpReads, boolean logError) {
        for(String valueName: valueNames) {
            ParentWithChild<HttpRead, HttpReadValue> read = HttpRead.getFirstHttpRead(valueName, httpReads);
            if(read != null) {
                logger.debug("{}: {} configured: read url={} data={} path={} extractionRegex={} factorToValue={}",
                        applianceId,
                        valueName,
                        read.parent().getUrl(),
                        read.child().getData(),
                        read.child().getPath(),
                        read.child().getExtractionRegex(),
                        read.child().getFactorToValue());
            } else {
                if(logError) {
                    logger.error("{}: Missing configuration for {}", applianceId, valueName);
                }
                return false;
            }
        }
        return true;
    }

    public boolean validateWrites(Collection<String> valueNames, List<HttpWrite> httpWrites) {
        for(EVWriteValueName valueName: EVWriteValueName.values()) {
            ParentWithChild<HttpWrite, HttpWriteValue> write = HttpWrite.getFirstHttpWrite(valueName.name(), httpWrites);
            if(write != null) {
                logger.debug("{}: {} configured: write url={} value={} factorToValue={}",
                        applianceId,
                        valueName.name(),
                        write.parent().getUrl(),
                        write.child().getValue(),
                        write.child().getFactorToValue());
            }
            else {
                logger.error("{}: Missing configuration for {}", applianceId, valueName.name());
                return false;
            }
        }
        return true;
    }
}
