package de.avanux.smartapplianceenabler.webservice;

import de.avanux.smartapplianceenabler.appliance.Schedule;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "Schedules")
@XmlAccessorType(XmlAccessType.FIELD)
public class Schedules {
    @XmlElement(name = "Schedule")
    private List<Schedule> schedules;

    public List<Schedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
    }
}
