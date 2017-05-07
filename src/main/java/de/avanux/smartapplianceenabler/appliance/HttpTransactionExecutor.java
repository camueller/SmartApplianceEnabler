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
package de.avanux.smartapplianceenabler.appliance;

import de.avanux.smartapplianceenabler.log.ApplianceLogger;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlTransient;
import java.io.IOException;

/**
 * Executor of a HTTP transaction.
 */
abstract public class HttpTransactionExecutor {

    @XmlTransient
    private ApplianceLogger logger = new ApplianceLogger(LoggerFactory.getLogger(HttpTransactionExecutor.class));

    /**
     * Send a HTTP request whose response has to be closed by the caller!
     * @param url
     * @return
     */
    protected CloseableHttpResponse sendHttpRequest(String url) {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        logger.debug("Sending request " + url);
        try {
            HttpRequestBase request = new HttpGet(url);
            CloseableHttpResponse response = client.execute(request);
            int responseCode = response.getStatusLine().getStatusCode();
            logger.debug("Response code is " + responseCode);
            return response;
        }
        catch(IOException e) {
            logger.error("Error executing HTTP request.", e);
            return null;
        }
    }
}
