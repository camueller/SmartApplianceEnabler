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

import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.*;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class HolidaysDownloader {
    private Logger logger = LoggerFactory.getLogger(HolidaysDownloader.class);
    public static final String urlConfigurationParamName = "Holidays.Url";
    private String url = "https://feiertage-api.de/api/?jahr={0}&nur_land=NATIONAL";

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
        try {
            /**
             * Avoid
             *
             * javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException:
             * PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException:
             * unable to find valid certification path to requested target
             *
             * temporary replacing the trust manager with an all-trusting one
             */
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){
                public X509Certificate[] getAcceptedIssuers(){return null;}
                public void checkClientTrusted(X509Certificate[] certs, String authType){}
                public void checkServerTrusted(X509Certificate[] certs, String authType){}
            }};
            // Install SSLSocketFactory with all-trusting trust manager
            SSLSocketFactory originalSSLSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());


            String resolvedUrl = getResolvedUrl();
            logger.debug("Will download holidays from " + resolvedUrl);
            Genson genson = new GensonBuilder().useRuntimeType(true).create();

            String out = new Scanner(new URL(resolvedUrl).openStream(), "UTF-8").useDelimiter("\\A").next();
            logger.debug("Holidays JSON: " + out);
            Reader reader = new StringReader(out);

            Map<String, Object> json = genson.deserialize(reader, Map.class);
            for(String holidayName : json.keySet()) {
                Map<String, Object> dateAndHint = (Map<String, Object>) json.get(holidayName);
                String datumString = dateAndHint.get("datum").toString();
                LocalDate datum = dateFormatter.parseLocalDate(datumString);
                holidayWithName.put(datum, holidayName);
            }

            // Restore original SSLSocketFactory
            HttpsURLConnection.setDefaultSSLSocketFactory(originalSSLSocketFactory);
        }
        catch(Exception e) {
            logger.error("Error downloading holidays", e);
        }
        logger.debug("Number of holidays: " + holidayWithName.size());
        return holidayWithName;
    }
}