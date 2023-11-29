/*
 * Copyright (C) 2017 Axel Müller <axel.mueller@avanux.de>
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

package de.avanux.smartapplianceenabler.control;

import de.avanux.smartapplianceenabler.TestBase;
import de.avanux.smartapplianceenabler.meter.Meter;
import de.avanux.smartapplianceenabler.mqtt.*;
import de.avanux.smartapplianceenabler.util.GuardedTimerTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

import static de.avanux.smartapplianceenabler.control.WrappedControl.WRAPPED_CONTROL_TOPIC;
import static org.mockito.Mockito.*;

public class StartingCurrentSwitchTest extends TestBase {
    private StartingCurrentSwitch sut;
    private StartingCurrentSwitchListener startingCurrentSwitchListener;
    private Control control;
    private MqttClient mqttClient;
    private InOrder mqttClientInOrder;
    private GuardedTimerTask mqttPublishTimerTask;

    private Timer timer = mock(Timer.class);
    private LocalDateTime now;

    @BeforeEach
    void setup() {
        sut = new StartingCurrentSwitch();

        now = LocalDateTime.now();

        mqttClient = spy(new MqttClientMock(applianceId, StartingCurrentSwitch.class));
        mqttClientInOrder = inOrder(mqttClient);

        sut = spy(new StartingCurrentSwitch());
        sut.setApplianceId(getClass().getSimpleName());
        control = mock(Control.class);
        sut.setControl(control);
        sut.setMqttClient(mqttClient);

        Mockito.doAnswer(invocation -> {
            mqttPublishTimerTask = (GuardedTimerTask) invocation.getArgument(0);
            mqttPublishTimerTask.runTask(now);
            return null;
        }).when(timer).schedule(Mockito.any(TimerTask.class), Mockito.eq(20000L), Mockito.eq(20000L));

        sut.init();
        sut.start(now, timer);
    }

    @Test
    public void detectStartingCurrent() throws Exception {
        // ... the appliance should be switched on
        verify(mqttClient).publish(WRAPPED_CONTROL_TOPIC, new ControlMessage(now, true), true, false);
        mqttClient.publishMessage(fullTopic(applianceId, WRAPPED_CONTROL_TOPIC), new ControlMessage(now, true), false);

        // ... but from the outside perspective the control is switched off
        mqttClientInOrder.verify(mqttClient).publish(Control.TOPIC, new StartingCurrentSwitchMessage(now, false, 15, 30, 300), false);
        verify(timer).schedule(any(TimerTask.class), eq(20000L), eq(20000L));

        mqttClient.publishMessage(fullTopic(applianceId, Meter.TOPIC), new MeterMessage(now, 0, 0), false);
        verify(mqttClient, never()).publish(MqttEventName.WrappedControlSwitchOnDetected.toString(), new MqttMessage(now), true, false);

        now = now.plusSeconds(10);
        mqttClient.publishMessage(fullTopic(applianceId, Meter.TOPIC), new MeterMessage(now, 10, 0), false);
        verify(mqttClient, never()).publish(eq(MqttEventName.WrappedControlSwitchOnDetected), any(MqttMessage.class));

        now = now.plusSeconds(30);
        mqttClient.publishMessage(fullTopic(applianceId, Meter.TOPIC), new MeterMessage(now, 20, 0), false);
        verify(mqttClient, never()).publish(eq(MqttEventName.WrappedControlSwitchOnDetected), any(MqttMessage.class));

        now = now.plusSeconds(31);
        mqttClient.publishMessage(fullTopic(applianceId, Meter.TOPIC), new MeterMessage(now, 31, 0), false);
        verify(mqttClient).publish(eq(MqttEventName.WrappedControlSwitchOnDetected), any(MqttMessage.class));

        // power threshold exceeded for more than configured starting current detection duration
        // ... causing appliance power off
        verify(mqttClient).publish(WRAPPED_CONTROL_TOPIC, new ControlMessage(now, false), true, false);
        // ... and also from the outside perspective the control is switched off
        mqttPublishTimerTask.runTask(now);
        mqttClientInOrder.verify(mqttClient).publishMessage(fullTopic(applianceId, Control.TOPIC), new StartingCurrentSwitchMessage(now, false, 15, 30, 300), false);
    }

    @Test
    public void detectFinishedCurrent() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        mqttClient.publishMessage(fullTopic(applianceId, Control.TOPIC + "/set"), new ControlMessage(now, true), false);
        mqttClient.publishMessage(fullTopic(applianceId, WRAPPED_CONTROL_TOPIC), new ControlMessage(now, true), false);

        // ... from the outside perspective the control is switched on
        mqttClientInOrder.verify(mqttClient).publish(Control.TOPIC, new StartingCurrentSwitchMessage(now, false, 15, 30, 300), false);

        now = now.plusSeconds(10);
        mqttClient.publishMessage(fullTopic(applianceId, Meter.TOPIC), new MeterMessage(now, 50, 0), false);
        verify(mqttClient, never()).publish(eq(MqttEventName.WrappedControlSwitchOffDetected), any(MqttMessage.class));

        now = now.plusSeconds(30);
        mqttClient.publishMessage(fullTopic(applianceId, Meter.TOPIC), new MeterMessage(now, 10, 0), false);
        verify(mqttClient, never()).publish(eq(MqttEventName.WrappedControlSwitchOffDetected), any(MqttMessage.class));

        // with minRunningTime not yet reached ...
        now = now.plusSeconds(331);
        mqttClient.publishMessage(fullTopic(applianceId, Meter.TOPIC), new MeterMessage(now, 10, 0), false);
        // ... no WrappedControlSwitchOffDetected is sent
        verify(mqttClient, never()).publish(eq(MqttEventName.WrappedControlSwitchOffDetected), any(MqttMessage.class));

        // after minRunningTime reached ...
        now = now.plusSeconds(231);
        mqttClient.publishMessage(fullTopic(applianceId, Meter.TOPIC), new MeterMessage(now, 10, 0), false);
        // ... WrappedControlSwitchOffDetected is sent
        verify(mqttClient).publish(eq(MqttEventName.WrappedControlSwitchOffDetected), any(MqttMessage.class));
    }
}
