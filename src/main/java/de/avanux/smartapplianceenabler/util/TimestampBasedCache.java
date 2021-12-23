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

package de.avanux.smartapplianceenabler.util;

import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class TimestampBasedCache<T> implements ApplianceIdConsumer {

    private Logger logger = LoggerFactory.getLogger(TimestampBasedCache.class);
    private String name;
    private TreeMap<LocalDateTime,T> timestampWithValue = new TreeMap<>();
    private String applianceId;
    private int maxAgeSeconds;
    private int keepLastExpired;

    public TimestampBasedCache(String name) {
        this.name = name;
    }

    public void setMaxAgeSeconds(int maxAgeSeconds) {
        this.maxAgeSeconds = maxAgeSeconds;
    }

    public int getMaxAgeSeconds() {
        return maxAgeSeconds;
    }

    public void setKeepLastExpired(int entriesToKeep) {
        this.keepLastExpired = entriesToKeep;
    }

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    public void addValue(LocalDateTime timestamp, T value) {
        // remove expired values
        List<LocalDateTime> expiredTimestamps = new ArrayList<>();
        List<LocalDateTime> timestamps = new ArrayList<>(timestampWithValue.keySet());
        timestamps.forEach(cachedTimeStamp -> {
            if(Duration.between(cachedTimeStamp, timestamp).toSeconds() > maxAgeSeconds) {
                expiredTimestamps.add(cachedTimeStamp);
            }
        });
        if(keepLastExpired > 0 && expiredTimestamps.size() > 0) {
            List<LocalDateTime> expiredTimestampsToKeep = new ArrayList<>();
            Collections.reverse(expiredTimestamps);
            for(int i=0; i<keepLastExpired; i++) {
                expiredTimestampsToKeep.add(expiredTimestamps.get(i));
            }
            expiredTimestamps.removeAll(expiredTimestampsToKeep);
        }
        expiredTimestamps.forEach(expiredTimestamp -> timestampWithValue.remove(expiredTimestamp));
        // add new value
        timestampWithValue.put(timestamp, value);
        logger.trace("{}: cache={} added value={} timestamp={}  removed/total: {}/{}",
                applianceId, name, value, timestamp, expiredTimestamps.size(), timestampWithValue.size());
    }

    public void clear() {
        this.timestampWithValue.clear();
    }

    public T getLastValue() {
        if(this.timestampWithValue.size() == 0) {
            return null;
        }
        Vector<LocalDateTime> keys = new Vector<>(this.timestampWithValue.keySet());
        return this.timestampWithValue.get(keys.lastElement());
    }

    public TreeMap<LocalDateTime, T> getTimestampWithValue() {
        return timestampWithValue;
    }

    public Map<LocalDateTime, T> getNotExpiredTimestampWithValue(LocalDateTime now) {
        return timestampWithValue.entrySet().stream()
                .filter(entry -> entry.getKey().isAfter(now.minusSeconds(maxAgeSeconds)))
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
    }
}
