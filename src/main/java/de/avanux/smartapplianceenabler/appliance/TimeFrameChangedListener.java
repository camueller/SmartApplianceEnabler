package de.avanux.smartapplianceenabler.appliance;

/**
 * Implementors will be notified when the current time frame has changed.
 */
public interface TimeFrameChangedListener {

    void timeFrameChanged(String applianceId, TimeFrame oldTimeFrame, TimeFrame newTimeFrame);

}
