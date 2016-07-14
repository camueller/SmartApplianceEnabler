package de.avanux.smartapplianceenabler.appliance;

import de.avanux.smartapplianceenabler.log.ApplianceLogger;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import java.io.IOException;
import java.util.Timer;

/**
 * Electricity meter reading power consumption from the response of a HTTP request.
 * <p>
 * IMPORTANT: The URLs in Appliance.xml have to be escaped (e.g. use "&amp;" instead of "&")
 */
public class HttpElectricityMeter extends HttpTransactionExecutor implements Meter, PollPowerExecutor {
    @XmlTransient
    private ApplianceLogger logger = new ApplianceLogger(LoggerFactory.getLogger(HttpElectricityMeter.class));
    @XmlAttribute
    private String url;
    @XmlAttribute
    private Float factorToWatt = 1.0f;
    @XmlAttribute
    private Integer measurementInterval = 60; // seconds
    @XmlAttribute
    private Integer pollInterval = 10; // seconds
    @XmlTransient
    private PollElectricityMeter pollElectricityMeter = new PollElectricityMeter();

    @Override
    public int getAveragePower() {
        int power = pollElectricityMeter.getAveragePower();
        logger.debug("average power = " + power + "W");
        return power;
    }

    @Override
    public int getMinPower() {
        int power = pollElectricityMeter.getMinPower();
        logger.debug("min power = " + power + "W");
        return power;
    }

    @Override
    public int getMaxPower() {
        int power = pollElectricityMeter.getMaxPower();
        logger.debug("max power = " + power + "W");
        return power;
    }

    @Override
    public Integer getMeasurementInterval() {
        return measurementInterval;
    }

    @Override
    public boolean isOn() {
        return getPower() > 0;
    }

    public void start(Timer timer) {
        pollElectricityMeter.start(timer, pollInterval, measurementInterval, this);
    }

    @Override
    public float getPower() {
        CloseableHttpResponse response = null;
        try {
            response = sendHttpRequest(url);
            if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String responseString = EntityUtils.toString(response.getEntity());
                return Float.parseFloat(responseString) * factorToWatt;
            }
        } catch (Exception e) {
            logger.error("Error reading HTTP response", e);
        } finally {
            try {
                if(response != null) {
                    response.close();
                }
            } catch (IOException e) {
                logger.error("Error closing HTTP response", e);
            }
        }
        return 0;
    }
}
