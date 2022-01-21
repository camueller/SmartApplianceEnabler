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

package de.avanux.smartapplianceenabler.protocol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonContentProtocolHandlerTest {
    private ContentProtocolHandler contentProtocolHandler;

    public JsonContentProtocolHandlerTest() {
        this.contentProtocolHandler = new JsonContentProtocolHandler();
    }

    @Test
    public void readTasmotaEnergy() {
        String content = "{\"StatusSNS\":{\"Time\":\"2020-10-26T09:44:31\",\"ENERGY\":{\"TotalStartTime\":\"2020-10-25T20:39:07\",\"Total\":0.329,\"Yesterday\":0.329,\"Today\":0.123,\"Power\":0,\"ApparentPower\":0,\"ReactivePower\":0,\"Factor\":0.00,\"Voltage\":0,\"Current\":0.000}}}";
        String selector = "$.StatusSNS.ENERGY.Today";
        this.contentProtocolHandler.parse(content);
        assertEquals("0.123", this.contentProtocolHandler.readValue(selector));
    }

    @Test
    public void readJsonArray() {
        String content = "{\"StatusSNS\":[212,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0]}";
        String selector = "$.StatusSNS[3]";
        this.contentProtocolHandler.parse(content);
        assertEquals("1", this.contentProtocolHandler.readValue(selector));
    }
}
