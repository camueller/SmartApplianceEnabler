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

package de.avanux.smartapplianceenabler.meter;

import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import de.avanux.smartapplianceenabler.configuration.ConfigurationException;
import de.avanux.smartapplianceenabler.configuration.Validateable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PulsePowerMeter implements ApplianceIdConsumer, Validateable {
    private Logger logger = LoggerFactory.getLogger(PulsePowerMeter.class);
    private String applianceId;
    private List<LocalDateTime> impulseTimestamps = Collections.synchronizedList(new ArrayList<LocalDateTime>());
    private Integer impulsesPerKwh;

    @Override
    public void validate() throws ConfigurationException {
        logger.debug("{}: Validating configuration", applianceId);
        if(impulsesPerKwh == null) {
            logger.error("{}: impulses per kwh not set", applianceId);
            throw new ConfigurationException();
        }
        logger.debug("{}: configured: impulsesPerKwh={}", applianceId, impulsesPerKwh);
    }

    void setImpulsesPerKwh(Integer impulsesPerKwh) {
        this.impulsesPerKwh = impulsesPerKwh;
    }

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    protected void addTimestamp(LocalDateTime timestamp) {
        impulseTimestamps.add(timestamp);
    }

    private void maintainTimestamps(LocalDateTime now) {
        List<LocalDateTime> impulseTimestampsForRemoval = new ArrayList<LocalDateTime>();
        this.impulseTimestamps.stream()
                .filter(timestamp -> isTimestampExpired(now, timestamp))
                .forEach(impulseTimestampsForRemoval::add);

        // remove expired timestamps but keep the 2 most most recent ones
        LocalDateTime secondMostRecentTimestamp = getSecondMostRecentTimestamp();
        LocalDateTime mostRecentTimestamp = getMostRecentTimestamp();
        this.impulseTimestamps.removeAll(impulseTimestampsForRemoval);
        if(this.impulseTimestamps.size() == 1) {
            if(secondMostRecentTimestamp != null) {
                this.impulseTimestamps.add(0, secondMostRecentTimestamp);
            }
        }
        else if(this.impulseTimestamps.size() == 0) {
            if(secondMostRecentTimestamp != null) {
                this.impulseTimestamps.add(secondMostRecentTimestamp);
            }
            if(mostRecentTimestamp != null) {
                this.impulseTimestamps.add(mostRecentTimestamp);
            }
        }
    }

    private boolean isTimestampExpired(LocalDateTime now, LocalDateTime timestamp) {
        return Duration.between(timestamp, now).toMillis() > Meter.AVERAGING_INTERVAL * 1000;
    }

    private LocalDateTime getMostRecentTimestamp() {
        return this.impulseTimestamps.size() > 0
                ? this.impulseTimestamps.get(this.impulseTimestamps.size() - 1) : null;
    }

    private LocalDateTime getSecondMostRecentTimestamp() {
        return this.impulseTimestamps.size() > 1
                ? this.impulseTimestamps.get(this.impulseTimestamps.size() - 2) : null;
    }

    protected double calculatePower(LocalDateTime now, LocalDateTime timestamp1, LocalDateTime timestamp2) {
        long timestampDelta12 = Duration.between(timestamp1, timestamp2).toMillis();
        if(isTimestampExpired(now, timestamp2)) {
            long timestampDeltaNow2 = Duration.between(timestamp2, now).toMillis();
            if(timestampDeltaNow2 > timestampDelta12 * 1.5) {
                return 0.0;
            }
        }
        // 3600s * 1000 W/Kw / (timestampDelta ms * 1s/1000ms * imp/KWh)
        double power = 3600.0 * 1000.0 / (timestampDelta12 / 1000.0 * this.impulsesPerKwh);
//        logger.debug("{}: timestamp1={} timestamp2={} timestampDelta12={} power={}",
//                applianceId, timestamp1, timestamp2, timestampDelta12, power);
        return power;
    }

    public int getAveragePower() {
        return getAveragePower(LocalDateTime.now());
    }

    int getAveragePower(LocalDateTime now) {
        this.maintainTimestamps(now);
        if(this.impulseTimestamps.size() == 0 || this.impulseTimestamps.size() == 1) {
            return 0;
        }

        Double powerValuesSum = 0.0;
        for(int i=0; i<this.impulseTimestamps.size() - 1; i ++) {
            powerValuesSum += calculatePower(now, impulseTimestamps.get(i), impulseTimestamps.get(i + 1));
        }
        int power = Double.valueOf(powerValuesSum / (impulseTimestamps.size() - 1)).intValue();
//        logger.debug("{}: impulseTimestamps-1={} powerValuesSum={} power={}",
//                applianceId, impulseTimestamps.size() - 1, powerValuesSum, power);
        return power;
    }

    public int getMinPower() {
        return getMinPower(LocalDateTime.now());
    }

    int getMinPower(LocalDateTime now) {
        this.maintainTimestamps(now);
        if(this.impulseTimestamps.size() == 0) {
            return 0;
        }
        if(this.impulseTimestamps.size() == 1) {
            return getAveragePower(now);
        }

        double minPower = Double.MAX_VALUE;
        for(int i=0; i<this.impulseTimestamps.size() - 1; i ++) {
            double power = calculatePower(now, this.impulseTimestamps.get(i), this.impulseTimestamps.get(i + 1));
            if(power < minPower) {
                minPower = power;
            }
        }
        return Double.valueOf(minPower).intValue();
    }

    public int getMaxPower() {
        return getMaxPower(LocalDateTime.now());
    }

    int getMaxPower(LocalDateTime now) {
        this.maintainTimestamps(now);
        if(this.impulseTimestamps.size() == 0) {
            return 0;
        }
        if(this.impulseTimestamps.size() == 1) {
            return getAveragePower(now);
        }

        double maxPower = Double.MIN_VALUE;
        for(int i=0; i<this.impulseTimestamps.size() - 1; i ++) {
            double power = calculatePower(now, this.impulseTimestamps.get(i), this.impulseTimestamps.get(i + 1));
            if(power > maxPower) {
                maxPower = power;
            }
        }
        return Double.valueOf(maxPower).intValue();
    }
}
