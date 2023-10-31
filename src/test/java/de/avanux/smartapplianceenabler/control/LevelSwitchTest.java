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

package de.avanux.smartapplianceenabler.control;

import de.avanux.smartapplianceenabler.TestBase;
import de.avanux.smartapplianceenabler.mqtt.ControlMessage;
import de.avanux.smartapplianceenabler.mqtt.MqttClient;
import de.avanux.smartapplianceenabler.mqtt.MqttClientMock;
import de.avanux.smartapplianceenabler.mqtt.VariablePowerConsumerMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Timer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LevelSwitchTest extends TestBase {
    private LevelSwitch levelSwitch = new LevelSwitch();
    private String applianceId = "F-001";
    private HttpSwitch httpSwitch1 = mock(HttpSwitch.class);
    private HttpSwitch httpSwitch2 = mock(HttpSwitch.class);
    private MqttClient mqttClient;
    private Timer timer = mock(Timer.class);
    private LocalDateTime now = LocalDateTime.now();


    public LevelSwitchTest() {
        levelSwitch = new LevelSwitch();

        mqttClient = spy(new MqttClientMock(applianceId, LevelSwitch.class));
        levelSwitch.setMqttClient(mqttClient);

        var controls = new ArrayList<Control>();
        controls.add(httpSwitch1);
        when(httpSwitch1.getId()).thenReturn("httpSwitch1");
        controls.add(httpSwitch2);
        when(httpSwitch2.getId()).thenReturn("httpSwitch2");

        var powerLevels = new ArrayList<PowerLevel>();

        var switchStatusPowerLevel1 = new ArrayList<SwitchStatus>();
        switchStatusPowerLevel1.add(new SwitchStatus("httpSwitch1", false));
        switchStatusPowerLevel1.add(new SwitchStatus("httpSwitch2", true));
        var powerLevel1 = new PowerLevel(1000, switchStatusPowerLevel1);
        powerLevels.add(powerLevel1);

        var switchStatusPowerLevel2 = new ArrayList<SwitchStatus>();
        switchStatusPowerLevel2.add(new SwitchStatus("httpSwitch1", true));
        switchStatusPowerLevel2.add(new SwitchStatus("httpSwitch2", false));
        var powerLevel2 = new PowerLevel(2000, switchStatusPowerLevel2);
        powerLevels.add(powerLevel2);

        levelSwitch.setPowerLevels(powerLevels);
        levelSwitch.setControls(controls);
        levelSwitch.setApplianceId("F-001");

        levelSwitch.init();
        levelSwitch.start(now, timer);
    }

    @Nested
    @DisplayName("isOn")
    class IsOn {
        @Test
        public void allWrappedControlsOff() {
            assertFalse(levelSwitch.isOn());
        }

        @Test
        public void oneWrappedControlsOn() {
            mqttClient.publishMessage(fullTopic("WrappedControl-httpSwitch1"), new ControlMessage(now, true), false);
            assertTrue(levelSwitch.isOn());
        }

        @Test
        public void allWrappedControlsOn() {
            mqttClient.publishMessage(fullTopic("WrappedControl-httpSwitch1"), new ControlMessage(now, true), false);
            mqttClient.publishMessage(fullTopic("WrappedControl-httpSwitch2"), new ControlMessage(now, false), false);
            assertTrue(levelSwitch.isOn());
        }
    }

    @Nested
    @DisplayName("setPower")
    class SetPower {
        @Test
        public void setPowerLevel1() {
            levelSwitch.setPower(now, 1000);
            verify(mqttClient).publish("WrappedControl-httpSwitch1", new ControlMessage(now, false), true, false);
            verify(mqttClient).publish("WrappedControl-httpSwitch2", new ControlMessage(now, true), true, false);
        }

        @Test
        public void setPowerLevel2() {
            levelSwitch.setPower(now, 2000);
            verify(mqttClient).publish("WrappedControl-httpSwitch1", new ControlMessage(now, true), true, false);
            verify(mqttClient).publish("WrappedControl-httpSwitch2", new ControlMessage(now, false), true, false);
        }

        @Test
        public void setPowerLevel1ViaMqtt() {
            mqttClient.publishMessage(fullTopic(Control.TOPIC + "/set"), new VariablePowerConsumerMessage(now, true, 1000, true), false);
            verify(mqttClient).publish("WrappedControl-httpSwitch1", new ControlMessage(now, false), true, false);
            verify(mqttClient).publish("WrappedControl-httpSwitch2", new ControlMessage(now, true), true, false);
        }

        @Test
        public void setPowerLevel2ViaMqtt() {
            mqttClient.publishMessage(fullTopic(Control.TOPIC + "/set"), new VariablePowerConsumerMessage(now, true, 2000, true), false);
            verify(mqttClient).publish("WrappedControl-httpSwitch1", new ControlMessage(now, true), true, false);
            verify(mqttClient).publish("WrappedControl-httpSwitch2", new ControlMessage(now, false), true, false);
        }
    }

    @Nested
    @DisplayName("getPower")
    class GetPower {
        @Test
        public void wrappedControl1On() {
            mqttClient.publishMessage(fullTopic("WrappedControl-httpSwitch1"), new ControlMessage(now, true), false);
            mqttClient.publishMessage(fullTopic("WrappedControl-httpSwitch2"), new ControlMessage(now, false), false);
            assertEquals(2000, levelSwitch.getPower());
        }

        @Test
        public void wrappedControl2On() {
            mqttClient.publishMessage(fullTopic("WrappedControl-httpSwitch1"), new ControlMessage(now, false), false);
            mqttClient.publishMessage(fullTopic("WrappedControl-httpSwitch2"), new ControlMessage(now, true), false);
            assertEquals(1000, levelSwitch.getPower());
        }
    }

    private String fullTopic(String topic) {
        return "sae/" + applianceId + "/" + topic;
    }
}
