package de.avanux.smartapplianceenabler.appliance;

import de.avanux.smartapplianceenabler.log.ApplianceLogger;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Changes the on/off state of an appliance by sending an HTTP request.
 * Limitation: Currently the appliance state is not requested by from the appliance but
 * maintained internally.
 */
public class HttpSwitch implements Control, ApplianceIdConsumer {
    @XmlTransient
    private ApplianceLogger logger = new ApplianceLogger(LoggerFactory.getLogger(HttpSwitch.class));
    @XmlAttribute
    private String onUrl;
    @XmlAttribute
    private String offUrl;
    @XmlTransient
    private boolean on;
    @XmlTransient
    List<ControlStateChangedListener> controlStateChangedListeners = new ArrayList<>();

    @Override
    public boolean on(boolean switchOn) {
        String url;
        if(switchOn) {
            url = onUrl;
        }
        else {
            url = offUrl;
        }
        boolean switchSuccessful = sendHttpRequest(url);
        if(switchSuccessful) {
            on = switchOn;
            for(ControlStateChangedListener listener : controlStateChangedListeners) {
                listener.controlStateChanged(switchOn);
            }
        }
        return switchSuccessful;
    }

    @Override
    public void addControlStateChangedListener(ControlStateChangedListener listener) {
        this.controlStateChangedListeners.add(listener);
    }

    @Override
    public void setApplianceId(String applianceId) {
        this.logger.setApplianceId(applianceId);
    }

    @Override
    public boolean isOn() {
        return on;
    }

    private boolean sendHttpRequest(String url) {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        logger.debug("Sending request " + url);
        try {
            HttpRequestBase request = new HttpGet(url);
            HttpResponse response = client.execute(request);
            int responseCode = response.getStatusLine().getStatusCode();
            logger.debug("Response code is " + responseCode);
            client.close();
            return responseCode == HttpStatus.SC_OK;
        }
        catch(IOException e) {
            logger.error("Error executing HTTP request.", e);
            return false;
        }
    }
}
