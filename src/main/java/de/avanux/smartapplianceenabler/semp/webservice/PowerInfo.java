package de.avanux.smartapplianceenabler.semp.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class PowerInfo {
    @XmlElement(name = "AveragePower")
    private Integer averagePower;
    @XmlElement(name = "MinPower")
    private Integer minPower;
    @XmlElement(name = "MaxPower")
    private Integer maxPower;
    @XmlElement(name = "Timestamp")
    private Integer timestamp;
    @XmlElement(name = "AveragingInterval")
    private Integer averagingInterval;

    public Integer getAveragePower() {
        return averagePower;
    }

    public void setAveragePower(Integer averagePower) {
        this.averagePower = averagePower;
    }

    public Integer getMinPower() {
        return minPower;
    }

    public void setMinPower(Integer minPower) {
        this.minPower = minPower;
    }

    public Integer getMaxPower() {
        return maxPower;
    }

    public void setMaxPower(Integer maxPower) {
        this.maxPower = maxPower;
    }

    public Integer getAveragingInterval() {
        return averagingInterval;
    }

    public void setAveragingInterval(Integer averagingInterval) {
        this.averagingInterval = averagingInterval;
    }

    public Integer getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Integer timestamp) {
        this.timestamp = timestamp;
    }
}
