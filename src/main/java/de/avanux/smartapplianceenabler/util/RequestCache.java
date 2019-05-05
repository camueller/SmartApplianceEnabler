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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class RequestCache<K, V> {
    private transient Logger logger = LoggerFactory.getLogger(RequestCache.class);
    private Map<K, CacheValue> cache = new HashMap<>();
    private int maxAgeMillis;
    private transient String applianceId;

    public RequestCache(String applianceId, int maxAgeSeconds) {
        this.applianceId = applianceId;
        this.maxAgeMillis = maxAgeSeconds * 1000;
        logger.debug("{}: Cache created maxAgeMillis={}", this.applianceId, this.maxAgeMillis);
    }

    public V get(K key) {
        CacheValue cacheValue = this.cache.get(key);
        if(cacheValue != null) {
            long ageMillis = System.currentTimeMillis() - cacheValue.timestamp;
            if(ageMillis < this.maxAgeMillis) {
                logger.debug("{}: Cache hit. size={} ageMillis={}", this.applianceId, this.cache.size(), ageMillis);
                return cacheValue.value;
            }
            else {
                logger.debug("{}: Cache entry expired. size={} ageMillis={}", this.applianceId, this.cache.size(), ageMillis);
            }
        }
        else {
            logger.debug("{}: Cache miss. size={}", this.applianceId, this.cache.size());
        }
        return null;
    }

    public void put(K key, V value) {
        CacheValue cacheValue = new CacheValue();
        cacheValue.value = value;
        cacheValue.timestamp = System.currentTimeMillis();
        this.cache.put(key, cacheValue);
    }

    public void clear() {
        this.cache.clear();
        logger.debug("{}: Cache cleared", this.applianceId);
    }

    private class CacheValue {
        public long timestamp;
        public V value;
    }
}
