package de.avanux.smartapplianceenabler.webservice;

import de.avanux.smartapplianceenabler.appliance.Schedule;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "Timeframes")
@XmlAccessorType(XmlAccessType.FIELD)
public class TimeFrames {
    @XmlElement(name = "Timeframe")
    private List<Schedule> schedules;

    public List<Schedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
    }
}
