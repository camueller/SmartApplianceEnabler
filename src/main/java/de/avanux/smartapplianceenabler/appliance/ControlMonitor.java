package de.avanux.smartapplianceenabler.appliance;

/**
 * A control monitor can read the control state.
 */
public interface ControlMonitor {

    /**
     * Returns the state of the control.
     * @return true, if the control state is "on"; otherwise false
     */
    boolean isOn();

}
