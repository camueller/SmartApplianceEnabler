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
 *
 * IMPORTANT: The URLs in Appliance.xml have to be escaped (e.g. use "&amp;" instead of "&")
 */
public class HttpSwitch extends HttpTransactionExecutor implements Control, ApplianceIdConsumer {
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
        HttpResponse response = sendHttpRequest(url);
        if(response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            on = switchOn;
            for(ControlStateChangedListener listener : controlStateChangedListeners) {
                listener.controlStateChanged(switchOn);
            }
            return true;
        }
        return false;
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
}
