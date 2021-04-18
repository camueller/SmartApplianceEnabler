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

import de.avanux.smartapplianceenabler.notification.NotificationHandler;
import de.avanux.smartapplianceenabler.notification.NotificationType;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Executor of a HTTP transaction.
 */
public class HttpTransactionExecutor {
    private Logger logger = LoggerFactory.getLogger(HttpTransactionExecutor.class);
    private String applianceId;
    private RequestConfig requestConfig;
    private HttpConfiguration configuration = new HttpConfiguration();
    private NotificationHandler notificationHandler = null;

    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    public void setConfiguration(HttpConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setNotificationHandler(NotificationHandler notificationHandler) {
        this.notificationHandler = notificationHandler;
    }

    public String execute(HttpMethod httpMethod, String url, String data) {
        CloseableHttpResponse response = null;
        try {
            response = executeLeaveOpen(httpMethod, url, data);
            if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return EntityUtils.toString(response.getEntity());
            }
        } catch (Exception e) {
            logger.error("{}: Error reading HTTP response", applianceId, e);
        } finally {
            closeResponse(response);
        }
        return null;
    }

    public CloseableHttpResponse executeLeaveOpen(HttpMethod httpMethod, String url, String data) {
        CloseableHttpResponse response = null;
        try {
            HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
            withUsernameAndPassword(httpClientBuilder, configuration.getUsername(), configuration.getPassword());
            CloseableHttpClient client = httpClientBuilder
                    .setDefaultRequestConfig(getRequestConfig())
                    .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false))
                    .build();
            try {
                HttpRequestBase request = null;
                if(httpMethod == null || httpMethod == HttpMethod.GET) {
                    request = new HttpGet(url);
                }
                else if (httpMethod == HttpMethod.POST) {
                    request = new HttpPost(url);
                }
                else if (httpMethod == HttpMethod.PATCH) {
                    request = new HttpPatch(url);
                }
                else if (httpMethod == HttpMethod.PUT) {
                    request = new HttpPut(url);
                }
                else if (httpMethod == HttpMethod.DELETE) {
                    request = new HttpDelete(url);
                }
                if(request != null) {
                    if(request instanceof HttpEntityEnclosingRequestBase) {
                        ((HttpEntityEnclosingRequestBase) request)
                                .setEntity(new StringEntity(data, configuration.getContentType()));
                    }
                    response = client.execute(request);
                    logResponse(response);
                }
            }
            catch(IOException e) {
                logger.error("{}: Error executing GET request.", applianceId, e);
                if(this.notificationHandler != null) {
                    this.notificationHandler.sendNotification(NotificationType.COMMUNICATION_ERROR);
                }
            }

            return response;
        } catch (Exception e) {
            logger.error("{}: Error reading HTTP response", applianceId, e);
        }
        return null;
    }

    protected HttpClientBuilder withUsernameAndPassword(HttpClientBuilder httpClientBuilder, String username, String password) {
        if(username != null && password != null) {
            logger.debug("{}: username={} password={}", applianceId, username, password);
            CredentialsProvider provider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
            provider.setCredentials(AuthScope.ANY, credentials);
            httpClientBuilder.setDefaultCredentialsProvider(provider);
        }
        return httpClientBuilder;
    }

    public void closeResponse(CloseableHttpResponse response) {
        try {
            if(response != null) {
                response.close();
            }
        } catch (IOException e) {
            logger.error("{}: Error closing HTTP response", applianceId, e);
        }
    }

    private CloseableHttpResponse logResponse(CloseableHttpResponse response) {
        int responseCode = response.getStatusLine().getStatusCode();
        logger.debug("{}: Response code is {}", applianceId, responseCode);
        return response;
    }

    private RequestConfig getRequestConfig() {
        if(this.requestConfig == null) {
            int timeout = 5; // seconds
            this.requestConfig = RequestConfig.custom()
                    .setConnectTimeout(timeout * 1000)
                    .setConnectionRequestTimeout(timeout * 1000)
                    .setSocketTimeout(timeout * 1000).build();

        }
        return this.requestConfig;
    }
}
