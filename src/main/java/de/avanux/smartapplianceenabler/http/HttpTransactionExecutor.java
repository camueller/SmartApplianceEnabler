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
package de.avanux.smartapplianceenabler.http;

import de.avanux.smartapplianceenabler.log.ApplianceLogger;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import java.io.IOException;

/**
 * Executor of a HTTP transaction.
 */
@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
abstract public class HttpTransactionExecutor {
    private transient ApplianceLogger logger = new ApplianceLogger(LoggerFactory.getLogger(HttpTransactionExecutor.class));
    @XmlAttribute
    private String contentType;
    @XmlAttribute
    private String username;
    @XmlAttribute
    private String password;

    public void setApplianceId(String applianceId) {
        this.logger.setApplianceId(applianceId);
    }

    protected ContentType getContentType() {
        if(contentType != null) {
            return ContentType.create(contentType);
        }
        return null;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    protected String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    protected String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Send a HTTP request whose response has to be closed by the caller!
     * @param url
     * @return
     */
    protected CloseableHttpResponse sendHttpRequest(String url, String data, ContentType contentType, String username, String password) {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        if(username != null && password != null) {
            CredentialsProvider provider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
            provider.setCredentials(AuthScope.ANY, credentials);
            httpClientBuilder.setDefaultCredentialsProvider(provider);
        }
        CloseableHttpClient client = httpClientBuilder.build();
        logger.debug("Sending HTTP request");
        logger.debug("url=" + url);
        logger.debug("data=" + data);
        logger.debug("contentType=" + contentType);
        logger.debug("username=" + username);
        logger.debug("password=" + password);
        try {
            HttpRequestBase request = null;
            if(data != null) {
                request = new HttpPost(url);
                ((HttpPost) request).setEntity(new StringEntity(data, contentType));
            }
            else {
                request = new HttpGet(url);
            }
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
