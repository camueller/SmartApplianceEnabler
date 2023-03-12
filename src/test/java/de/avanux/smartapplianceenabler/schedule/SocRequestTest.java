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

package de.avanux.smartapplianceenabler.schedule;

import de.avanux.smartapplianceenabler.TestBase;
import de.avanux.smartapplianceenabler.control.ev.SocValues;
import de.avanux.smartapplianceenabler.mqtt.EVChargerSocChangedEvent;
import de.avanux.smartapplianceenabler.mqtt.MqttEventName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SocRequestTest extends TestBase {
    private SocRequest sut;

    @BeforeEach
    void setup() {
        sut = new SocRequest();
        sut.setMqttClient(mqttClient);
        sut.setApplianceId("F-001");
        sut.setEvId(1);
    }

    @Test
    @DisplayName("without SOC value assume to charge from 0% to 100%")
    public void noSocValuesAvailable() {
        mqttMessageArrivedOnSubscribe(MqttEventName.EVChargerSocChanged,
                new EVChargerSocChangedEvent(LocalDateTime.now(), new SocValues(100000, null, null, null, null, null), 0.0)
        );

        sut.init();

        assertEquals(100000, sut.getMin(LocalDateTime.now()));
        assertEquals(100000, sut.getMax(LocalDateTime.now()));
    }

    @Nested
    @DisplayName("Target SOC available")
    class TargetSocAvailable {
        @BeforeEach
        void setup() {
            sut.setSoc(50);
        }

        @Test
        @DisplayName("with target SOC available assume to charge from 0% to target SOC")
        public void targetSocAvailable() {
            mqttMessageArrivedOnSubscribe(MqttEventName.EVChargerSocChanged,
                    new EVChargerSocChangedEvent(LocalDateTime.now(), new SocValues(100000, null, null, null, null, null), 0.0)
            );

            sut.init();

            assertEquals(50000, sut.getMin(LocalDateTime.now()));
            assertEquals(50000, sut.getMax(LocalDateTime.now()));
        }


        @Test
        @DisplayName("with current SOC and target SOC available assume to charge from current SOC to target SOC")
        public void currentSocAndTargetSocAvailable() {
            mqttMessageArrivedOnSubscribe(MqttEventName.EVChargerSocChanged,
                    new EVChargerSocChangedEvent(LocalDateTime.now(), new SocValues(100000, 10, null, null, null, 20), 0.0)
            );

            sut.init();

            assertEquals(30000, sut.getMin(LocalDateTime.now()));
            assertEquals(30000, sut.getMax(LocalDateTime.now()));
        }

    }


    @Test
    @DisplayName("without battery capacity available no energy can be calculated")
    public void noBatteryCapacityAvailable() {
        mqttMessageArrivedOnSubscribe(MqttEventName.EVChargerSocChanged,
                new EVChargerSocChangedEvent(LocalDateTime.now(), new SocValues(null, null, null, null, null, null), 0.0)
        );

        sut.init();

        assertNull(sut.getMin(LocalDateTime.now()));
        assertNull(sut.getMax(LocalDateTime.now()));
    }

}
