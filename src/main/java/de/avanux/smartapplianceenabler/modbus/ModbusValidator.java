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

package de.avanux.smartapplianceenabler.modbus;

import de.avanux.smartapplianceenabler.util.ParentWithChild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ModbusValidator {

    private transient Logger logger = LoggerFactory.getLogger(ModbusValidator.class);
    private transient String applianceId;

    public ModbusValidator(String applianceId) {
        this.applianceId = applianceId;
    }

    public boolean validateReads(String valueName, List<ParentWithChild<ModbusRead, ModbusReadValue>> reads) {
        if(reads.size() > 0) {
            for(ParentWithChild<ModbusRead, ModbusReadValue> read: reads) {
                ModbusRead registerRead = read.parent();
                logger.debug("{}: {} configured: read register={} / bytes={} / byte order={} / type={} / extraction regex={} / factorToValue={}",
                        applianceId,
                        valueName,
                        registerRead.getAddress(),
                        registerRead.getWords(),
                        registerRead.getByteOrder(),
                        registerRead.getType(),
                        read.child().getExtractionRegex(),
                        registerRead.getFactorToValue());
            }
            return true;
        }
        logger.error("{}: Missing register configuration for {}", applianceId, valueName);
        return false;
    }

    public boolean validateWrites(String valueName, List<ParentWithChild<ModbusWrite, ModbusWriteValue>> writes) {
        if(writes.size() > 0) {
            for(ParentWithChild<ModbusWrite, ModbusWriteValue> write: writes) {
                logger.debug("{}: {} configured: write register={} value={} factorToValue={}",
                        applianceId,
                        valueName,
                        write.parent().getAddress(),
                        write.child().getValue(),
                        write.parent().getFactorToValue());
            }
            return true;
        }
        logger.error("{}: Missing register configuration for {}", applianceId, valueName);
        return false;
    }
}
