package de.avanux.smartapplianceenabler.appliance;

import org.joda.time.Interval;

/**
 * A TimeframeInterval associates a timeframe with an interval.
 */
public class TimeframeInterval {
    private Timeframe timeframe;
    private Interval interval;

    public TimeframeInterval(Timeframe timeframe, Interval interval) {
        this.timeframe = timeframe;
        this.interval = interval;
    }

    public Timeframe getTimeframe() {
        return timeframe;
    }

    public Interval getInterval() {
        return interval;
    }

    @Override
    public String toString() {
        String text = "";
        if(interval != null) {
            text += interval.toString();
        }
        if(timeframe != null) {
            if (interval != null) {
                text += "(";
            }
            text += timeframe.toString();
            if (interval != null) {
                text += ")";
            }
        }
        return text;
    }
}
