/*
 * Copyright (C) 2022 Axel Müller <axel.mueller@avanux.de>
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

import javax.net.ssl.*;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Scanner;

abstract public class Downloader {

    private Logger logger = LoggerFactory.getLogger(Downloader.class);

    protected String downloadAsString(String url) {
        String content = null;
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

            var inputStream = new URL(url).openStream();
            content = new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();
            inputStream.close();

            // Restore original SSLSocketFactory
            HttpsURLConnection.setDefaultSSLSocketFactory(originalSSLSocketFactory);
        }
        catch(Exception e) {
            logger.error("Error downloading from {}", url, e);
        }
        return content;
    }

}
