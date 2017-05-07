package de.avanux.smartapplianceenabler.appliance;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Created by axel on 07.05.17.
 */
public class UseSchedule {
    @XmlElement(name = "DetectedAfter")
    private TimeOfDay detectedAfter;
    @XmlElement(name = "DetectedBefore")
    private TimeOfDay detectedBefore;
    @XmlAttribute
    private String idref;

    public String getIdref() {
        return idref;
    }

    public TimeOfDay getDetectedAfter() {
        return detectedAfter;
    }

    public void setDetectedAfter(TimeOfDay detectedAfter) {
        this.detectedAfter = detectedAfter;
    }

    public TimeOfDay getDetectedBefore() {
        return detectedBefore;
    }

    public void setDetectedBefore(TimeOfDay detectedBefore) {
        this.detectedBefore = detectedBefore;
    }
}
