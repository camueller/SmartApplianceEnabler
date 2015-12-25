package de.avanux.smartapplianceenabler.appliance;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class TimeFrame {
    @XmlElement(name = "EarliestStart")
    EarliestStart earliestStart;
    @XmlElement(name = "LatestEnd")
    LatestEnd latestEnd;

    public EarliestStart getEarliestStart() {
        return earliestStart;
    }

    public void setEarliestStart(EarliestStart earliestStart) {
        this.earliestStart = earliestStart;
    }

    public LatestEnd getLatestEnd() {
        return latestEnd;
    }

    public void setLatestEnd(LatestEnd latestEnd) {
        this.latestEnd = latestEnd;
    }
}
