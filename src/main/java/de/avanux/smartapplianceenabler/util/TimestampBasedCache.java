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

import java.util.*;

public class TimestampBasedCache<T> implements ApplianceIdConsumer {

    private Logger logger = LoggerFactory.getLogger(TimestampBasedCache.class);
    private String name;
    private TreeMap<Long,T> timestampWithValue = new TreeMap<>();
    private String applianceId;
    private int maxAgeSeconds;

    public TimestampBasedCache(String name) {
        this.name = name;
    }

    public void setMaxAgeSeconds(int maxAgeSeconds) {
        logger.debug("{}: maxAgeSeconds={}", applianceId, maxAgeSeconds);
        this.maxAgeSeconds = maxAgeSeconds;
    }

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    public void addValue(Long timestamp, T value) {
        // remove expired values
        Set<Long> expiredTimestamps = new HashSet<Long>();
        for(Long cachedTimeStamp : timestampWithValue.keySet()) {
            if(cachedTimeStamp < timestamp - maxAgeSeconds * 1000) {
                expiredTimestamps.add(cachedTimeStamp);
            }
        }
        for(Long expiredTimestamp : expiredTimestamps) {
            timestampWithValue.remove(expiredTimestamp);
        }
        // add new value
        timestampWithValue.put(timestamp, value);
        logger.debug("{}: cache={} added value={} timestamp={}  removed/total: {}/{}",
                applianceId, name, value, timestamp, expiredTimestamps.size(), timestampWithValue.size());
    }

    public boolean isEmpty() {
        return this.timestampWithValue.isEmpty();
    }

    public List<T> values() {
        List<T> values = new ArrayList<>();
        values.addAll(this.timestampWithValue.values());
        return values;
    }

    public T getLastValue() {
        if(size() == 0) {
            return null;
        }
        Vector<Long> keys = new Vector<>(this.timestampWithValue.keySet());
        return this.timestampWithValue.get(keys.lastElement());
    }

    public TreeMap<Long, T> getTimestampWithValue() {
        return timestampWithValue;
    }

    public int size() {
        return this.timestampWithValue.size();
    }
}
