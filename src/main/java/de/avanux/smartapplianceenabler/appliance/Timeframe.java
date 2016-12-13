package de.avanux.smartapplianceenabler.appliance;

import org.joda.time.Interval;
import org.joda.time.LocalDateTime;

import java.util.List;

/**
 * A timeframe belongs to a schedule and consists of one or more intervals.
 */
public interface Timeframe {

    /**
     * Set the schedule associated with this timeframe.
     * @param schedule
     */
    void setSchedule(Schedule schedule);

    /**
     * Returns the schedule associated with this timeframe.
     * @return
     */
    Schedule getSchedule();

    /**
     * Return all intervals of this timeframe.
     * @param now
     * @return a (possibly empty) list; never null
     */
    List<TimeframeInterval> getIntervals(LocalDateTime now);
}
