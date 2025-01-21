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
package de.avanux.smartapplianceenabler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.avanux.smartapplianceenabler.mqtt.MqttMessage;
import de.avanux.smartapplianceenabler.util.Downloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.io.StringReader;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class HolidaysDownloader extends Downloader {
    private Logger logger = LoggerFactory.getLogger(HolidaysDownloader.class);
    public transient static final String DEFAULT_URL = "https://feiertage-api.de/api/?jahr={0}&nur_land=NATIONAL";
    private String url;

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        logger.debug("Holidays download URL set to " + this.url);
    }

    private String getResolvedUrl() {
        int year = LocalDate.now().getYear();
        return MessageFormat.format(url != null ? url : DEFAULT_URL, Integer.valueOf(year).toString());
    }

    public Map<LocalDate, String> downloadHolidays() {
        Map<LocalDate, String> holidayWithName = new LinkedHashMap<>();
        try {
            String resolvedUrl = getResolvedUrl();
            logger.debug("Will download holidays from " + resolvedUrl);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            String out = downloadAsString(resolvedUrl);
            logger.debug("Holidays JSON: " + out);
            Reader reader = new StringReader(out);

            Map<String, Object> json = gson.fromJson(reader, Map.class);
            for(String holidayName : json.keySet()) {
                Map<String, Object> dateAndHint = (Map<String, Object>) json.get(holidayName);
                String datumString = dateAndHint.get("datum").toString();
                LocalDate datum = LocalDate.parse(datumString, dateFormatter);
                holidayWithName.put(datum, holidayName);
            }
        }
        catch(Exception e) {
            logger.error("Error downloading holidays", e);
        }
        logger.debug("Number of holidays: " + holidayWithName.size());
        return holidayWithName;
    }
}