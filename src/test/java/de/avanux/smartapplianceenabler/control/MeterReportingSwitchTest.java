/*
 * Copyright (C) 2020 Axel Müller <axel.mueller@avanux.de>
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

import de.avanux.smartapplianceenabler.meter.Meter;
import de.avanux.smartapplianceenabler.notification.NotificationHandler;
import de.avanux.smartapplianceenabler.notification.NotificationType;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.Timer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class MeterReportingSwitchTest {
    private MeterReportingSwitch meterReportingSwitch;
    private Meter meter = mock(Meter.class);
    private NotificationHandler notificationHandler = mock(NotificationHandler.class);
    private LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void beforeEach() {
//        when(meter.getAveragePower()).thenReturn(0);
        meterReportingSwitch = new MeterReportingSwitch();
//        meterReportingSwitch.addControlStateChangedListener(controlStateChangedListener);
        meterReportingSwitch.setNotificationHandler(notificationHandler);
        meterReportingSwitch.setApplianceId("F-001");
        meterReportingSwitch.init();
        meterReportingSwitch.start(now, mock(Timer.class));
    }

    @AfterEach
    void afterEach() {
        meterReportingSwitch.stop(now);
//        meterReportingSwitch.removeControlStateChangedListener(controlStateChangedListener);
    }

    @Nested
    @DisplayName("initially")
    class initially {
        @Test
        @DisplayName("it reports to be switched off")
        void isOn_initially() {
            assertFalse(meterReportingSwitch.isOn(now));
        }

        @Test
        @DisplayName("it reports to be not controllable")
        void isControllable() {
            assertFalse(meterReportingSwitch.isControllable());
        }

        @Test
        @DisplayName("it does not call ControlStateChangedListener")
        void isOn_doesNotCallControlStateChangedListener() {
            meterReportingSwitch.isOn(now);
//            verify(controlStateChangedListener, never()).controlStateChanged(any(), anyBoolean());
        }

//        @Test
//        @DisplayName("it reports to be switched off if average power is below threshold")
//        void isOn_belowThreshold() {
//            when(meter.getAveragePower()).thenReturn(MeterReportingSwitchDefaults.getPowerThreshold() - 1);
//            assertFalse(meterReportingSwitch.isOn(now));
//        }
    }

//    @Nested
//    @DisplayName("switch on")
//    class switchOn {
//        @BeforeEach
//        void beforeEach() {
//            meterReportingSwitch.setOnBefore(false);
//            when(meter.getAveragePower()).thenReturn(MeterReportingSwitchDefaults.getPowerThreshold() + 1);
//        }
//
//        @Test
//        @DisplayName("it reports to be switched on if average power is above threshold")
//        void isOn_aboveThreshold() {
//            assertTrue(meterReportingSwitch.isOn(now));
//        }
//
//        @Test
//        @DisplayName("it calls ControlStateChangedListener")
//        void isOn_doesNotCallControlStateChangedListener() {
//            meterReportingSwitch.isOn(now);
//            verify(controlStateChangedListener).controlStateChanged(now, true);
//        }
//
//        @Test
//        @DisplayName("it sends notification if switched on")
//        void isOn_sendNotification() {
//            meterReportingSwitch.isOn(now);
//            verify(notificationHandler).sendNotification(NotificationType.CONTROL_ON);
//        }
//    }
//
//    @Nested
//    @DisplayName("switch off (no off detection delay)")
//    class switchOffNoOffDetectionDelay {
//        @BeforeEach
//        void beforeEach() {
//            meterReportingSwitch.setOnBefore(true);
//            when(meter.getAveragePower()).thenReturn(MeterReportingSwitchDefaults.getPowerThreshold() - 1);
//        }
//
//        @Test
//        @DisplayName("it reports to be switched on if average power is below threshold")
//        void isOn_belowThreshold_withinCheckInterval() {
//            assertFalse(meterReportingSwitch.isOn(now));
//        }
//
//        @Test
//        @DisplayName("it calls ControlStateChangedListener")
//        void isOn_doesNotCallControlStateChangedListener() {
//            meterReportingSwitch.isOn(now);
//            verify(controlStateChangedListener).controlStateChanged(now, false);
//        }
//
//        @Test
//        @DisplayName("it sends notification if switched off")
//        void isOn_sendNotification() {
//            meterReportingSwitch.isOn(now);
//            verify(notificationHandler).sendNotification(NotificationType.CONTROL_OFF);
//        }
//    }
//
//    @Nested
//    @DisplayName("switch off (with off detection delay)")
//    class switchOffWithOffDetectionDelay {
//        private final int offDetectionDelay = 10;
//
//        @BeforeEach
//        void beforeEach() {
//            meterReportingSwitch.setOnBefore(true);
//            meterReportingSwitch.setLastOn(now);
//            meterReportingSwitch.setOffDetectionDelay(offDetectionDelay);
//            when(meter.getAveragePower()).thenReturn(MeterReportingSwitchDefaults.getPowerThreshold() - 1);
//        }
//
//        @Nested
//        @DisplayName("within check interval")
//        class WithinCheckInterval {
//            LocalDateTime timeWithinCheckInterval = now.plusSeconds(offDetectionDelay - 1);
//
//            @Test
//            @DisplayName("it reports to be switched on if average power is below threshold")
//            void isOn_belowThreshold_withinCheckInterval() {
//                assertTrue(meterReportingSwitch.isOn(timeWithinCheckInterval));
//            }
//
//            @Test
//            @DisplayName("it calls ControlStateChangedListener")
//            void isOn_doesNotCallControlStateChangedListener() {
//                meterReportingSwitch.isOn(now);
//                verify(controlStateChangedListener, never()).controlStateChanged(now, false);
//            }
//
//            @Test
//            @DisplayName("it does not send notification if average power is below threshold")
//            void isOn_doesNotSendNotification() {
//                meterReportingSwitch.isOn(now);
//                verify(notificationHandler, never()).sendNotification(NotificationType.CONTROL_OFF);
//            }
//        }
//
//        @Nested
//        @DisplayName("after check interval")
//        class AfterCheckInterval {
//            LocalDateTime timeAfterCheckInterval = now.plusSeconds(offDetectionDelay + 1);
//
//            @Test
//            @DisplayName("it reports to be switched off if average power is below threshold")
//            void isOn_belowThreshold_afterCheckInterval() {
//                assertFalse(meterReportingSwitch.isOn(timeAfterCheckInterval));
//            }
//
//            @Test
//            @DisplayName("it calls ControlStateChangedListener")
//            void isOn_doesNotCallControlStateChangedListener() {
//                meterReportingSwitch.isOn(timeAfterCheckInterval);
//                verify(controlStateChangedListener).controlStateChanged(timeAfterCheckInterval, false);
//            }
//
//            @Test
//            @DisplayName("it sends notification if average power is below threshold")
//            void isOn_doesNotSendNotification() {
//                meterReportingSwitch.isOn(timeAfterCheckInterval);
//                verify(notificationHandler).sendNotification(NotificationType.CONTROL_OFF);
//            }
//        }
//    }
}
