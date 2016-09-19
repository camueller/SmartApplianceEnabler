package de.avanux.smartapplianceenabler.appliance;

import de.avanux.smartapplianceenabler.log.ApplianceLogger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A PollElectricityMeter calculates power consumption by polling.
 */
public class PollElectricityMeter implements ApplianceIdConsumer {

    private ApplianceLogger logger = new ApplianceLogger(LoggerFactory.getLogger(PollElectricityMeter.class));
    private Map<Long,Float> timestampWithPower = new HashMap<Long,Float>();
    private Integer measurementInterval;

    @Override
    public void setApplianceId(String applianceId) {
        this.logger.setApplianceId(applianceId);
    }

    public void start(Timer timer, Integer pollInterval, Integer measurementInterval, PollPowerExecutor pollPowerExecutor) {
        this.measurementInterval = measurementInterval;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                addValue(pollPowerExecutor.getPower());
            }
        }, 0, pollInterval * 1000);
    }

    public void addValue(float power) {
        long currentTimestamp = System.currentTimeMillis();
        // remove expired values
        Set<Long> expiredTimestamps = new HashSet<Long>();
        for(Long cachedTimeStamp : timestampWithPower.keySet()) {
            if(cachedTimeStamp < currentTimestamp - measurementInterval * 1000) {
                expiredTimestamps.add(cachedTimeStamp);
            }
        }
        for(Long expiredTimestamp : expiredTimestamps) {
            timestampWithPower.remove(expiredTimestamp);
        }
        // add new value
        timestampWithPower.put(currentTimestamp, power);
        logger.debug("timestamps added/removed/total: 1/" + expiredTimestamps.size() + "/" + timestampWithPower.size());
    }

    public int getAveragePower() {
        double sum = 0.0f;
        if(!timestampWithPower.isEmpty()) {
            for (Float value : timestampWithPower.values()) {
                sum += value;
            }
            return Double.valueOf(sum / timestampWithPower.size()).intValue();
        }
        return 0;
    }

    public int getMinPower() {
        float minValue = Float.MAX_VALUE;
        if(!timestampWithPower.isEmpty()) {
            for (Float value : timestampWithPower.values()) {
                if(value < minValue) {
                    minValue = value;
                }
            }
            return Float.valueOf(minValue).intValue();
        }
        return 0;
    }

    public int getMaxPower() {
        float maxValue = Float.MIN_VALUE;
        if(!timestampWithPower.isEmpty()) {
            for (Float value : timestampWithPower.values()) {
                if(value > maxValue) {
                    maxValue = value;
                }
            }
            return Float.valueOf(maxValue).intValue();
        }
        return 0;
    }
}
