package de.avanux.smartapplianceenabler.webservice;

import de.avanux.smartapplianceenabler.appliance.TimeFrame;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "Timeframes")
@XmlAccessorType(XmlAccessType.FIELD)
public class TimeFrames {
    @XmlElement(name = "Timeframe")
    private List<TimeFrame> timeFrames;

    public List<TimeFrame> getTimeFrames() {
        return timeFrames;
    }

    public void setTimeFrames(List<TimeFrame> timeFrames) {
        this.timeFrames = timeFrames;
    }
}
