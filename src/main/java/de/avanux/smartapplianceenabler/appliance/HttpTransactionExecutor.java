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
