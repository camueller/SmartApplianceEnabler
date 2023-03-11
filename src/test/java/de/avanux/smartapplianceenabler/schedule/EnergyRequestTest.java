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
import de.avanux.smartapplianceenabler.meter.Meter;
import de.avanux.smartapplianceenabler.mqtt.MeterMessage;
import de.avanux.smartapplianceenabler.mqtt.MqttClient;
import de.avanux.smartapplianceenabler.mqtt.MqttMessage;
import de.avanux.smartapplianceenabler.mqtt.MqttMessageHandler;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
public class EnergyRequestTest extends TestBase {
    private EnergyRequest sut;
    private MqttClient mqttClient = Mockito.mock(MqttClient.class);
    private TimeframeIntervalStateProvider timeframeIntervalStateProvider = Mockito.mock(TimeframeIntervalStateProvider.class);

    public EnergyRequestTest() {
        sut = new EnergyRequest();
        sut.setMqttClient(mqttClient);
        sut.setTimeframeIntervalStateProvider(timeframeIntervalStateProvider);
    }

    @Test
    @DisplayName("don't decrease energyMetered because of metehr reset caused by timeframe interval change")
    public void init() {
        sut.setMin(5000);
        sut.setMax(10000);
        var mqttMessage1 = new MeterMessage(LocalDateTime.now(), 1000, 1.5);
        var mqttMessage2 = new MeterMessage(LocalDateTime.now(), 1000, 0);
        Mockito.doAnswer(invocation -> {
            var messageHandler = (MqttMessageHandler) invocation.getArgument(2);
            messageHandler.messageArrived(Meter.TOPIC, mqttMessage1);
            messageHandler.messageArrived(Meter.TOPIC, mqttMessage2);
            return null;
        }).when(mqttClient).subscribe(Mockito.any(String.class), Mockito.any(Boolean.class), Mockito.any(MqttMessageHandler.class));
        Mockito.when(timeframeIntervalStateProvider.getState()).thenReturn(TimeframeIntervalState.ACTIVE);

        sut.init();

        assertEquals(3500, sut.getMin(LocalDateTime.now()));
        assertEquals(8500, sut.getMax(LocalDateTime.now()));
    }
}
