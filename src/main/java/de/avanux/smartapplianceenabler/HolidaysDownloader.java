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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class HolidaysDownloader {
    private Logger logger = LoggerFactory.getLogger(HolidaysDownloader.class);
    public static final String urlConfigurationParamName = "Holidays.Url";
    private String url = "http://feiertage.jarmedia.de/api/?jahr={0}&nur_land=NATIONAL";
    private static final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        logger.debug("Holidays download URL set to " + this.url);
    }

    private String getResolvedUrl() {
        int year = new LocalDate().getYear();
        return MessageFormat.format(url, Integer.valueOf(year).toString());
    }

    public Map<LocalDate, String> downloadHolidays() {
        Map<LocalDate, String> holidayWithName = new LinkedHashMap<>();
        BufferedReader reader = null;
        try {
            JsonParser parser = new JsonParser();
            String resolvedUrl = getResolvedUrl();
            logger.debug("Will download holidays from " + resolvedUrl);
            URL url = new URL(resolvedUrl);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            JsonObject asJsonArray = parser.parse(reader).getAsJsonObject();
            final Set<Map.Entry<String, JsonElement>> entries = asJsonArray.entrySet();
            for(Map.Entry<String, JsonElement> x : entries) {
                String name = x.getKey();
                JsonObject datumUndHinweis = x.getValue().getAsJsonObject();
                String datumString = datumUndHinweis.get("datum").getAsString();
                LocalDate datum = dateFormatter.parseLocalDate(datumString);
                holidayWithName.put(datum, name);
            }
        }
        catch(Exception e) {
            logger.error("Error downloading holidays", e);
        }
        finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.error("Error closing reader", e);
                }
            }
        }
        logger.debug("Number of holidays: " + holidayWithName.size());
        return holidayWithName;
    }
}
