package de.avanux.smartapplianceenabler.appliance;

/**
 * Implementors will be notified when the active interval has changed.
 */
public interface ActiveIntervalChangedListener {

    void activeIntervalChanged(String applianceId, TimeframeInterval deactivatedInterval, TimeframeInterval activatedInterval);

}
