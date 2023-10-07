/*
 * Copyright (C) 2022 Axel Müller <axel.mueller@avanux.de>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package de.avanux.smartapplianceenabler.control.ev;

import de.avanux.smartapplianceenabler.appliance.ApplianceIdConsumer;
import de.avanux.smartapplianceenabler.mqtt.MeterMessage;
import de.avanux.smartapplianceenabler.schedule.Request;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.util.SloppyMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class ElectricVehicleHandler implements ApplianceIdConsumer, SocScriptExecutionResultListener {

    private transient Logger logger = LoggerFactory.getLogger(ElectricVehicleHandler.class);
    private DecimalFormat percentageFormat;
    private String applianceId;
    private List<ElectricVehicle> vehicles;
    private Map<Integer, SocScriptExecutor> evIdWithSocScriptExecutor = new HashMap<>();
    /**
     * Is only set if connected vehicle has been identified
     */
    private Integer connectedVehicleId;
    private Pair<Double, Double> evChargerLocation;
    private SocScriptExecutionResult previousSocScriptExecutionResult;
    private double socRetrievalEnergyMeterValue;
    private SocValues socValues;
    private SocValuesChangedListener socValuesChangedListener;
    private boolean socCalculationRequired;
    private double chargeLoss;
    private MeterMessage meterMessage;
    private boolean socRetrievalForChargingAlmostCompleted;
    private boolean chargingAlmostCompleted;
    private boolean socScriptAsync = true;

    public ElectricVehicleHandler() {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
        percentageFormat = (DecimalFormat) nf;
        percentageFormat.applyPattern("#'%'");

        onVehicleDisconnected();
    }

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    public void setSocScriptAsync(boolean socScriptAsync) {
        this.socScriptAsync = socScriptAsync;
    }

    protected Map<Integer, SocScriptExecutor> getEvIdWithSocScriptExecutor() {
        return evIdWithSocScriptExecutor;
    }

    public void setVehicles(List<ElectricVehicle> vehicles) {
        this.vehicles = vehicles;

        if(this.vehicles != null) {
            for(ElectricVehicle vehicle: this.vehicles) {
                vehicle.setApplianceId(applianceId);
            }
            for(ElectricVehicle vehicle: this.vehicles) {
                logger.debug("{}: {}", this.applianceId, vehicle);
            }
            this.vehicles.forEach(vehicle -> {
                if(vehicle.getSocScript() != null) {
                    SocScriptExecutor executor = new SocScriptExecutor(vehicle.getId(), vehicle.getSocScript());
                    executor.setApplianceId(this.applianceId);
                    this.evIdWithSocScriptExecutor.put(vehicle.getId(), executor);
                }
            });
            this.chargeLoss = getConnectedVehicle().getChargeLoss() != null
                    ? getConnectedVehicle().getChargeLoss().floatValue() : 0.0f;

        }
    }

    public void setEvChargerLocation(Pair<Double, Double> evChargerLocation) {
        this.evChargerLocation = evChargerLocation;
    }

    public void setMeterMessage(MeterMessage meterMessage) {
        this.meterMessage = meterMessage;
    }

    public void onVehicleDisconnected() {
        this.connectedVehicleId = null;
        this.socValues = new SocValues();
        this.socRetrievalForChargingAlmostCompleted = false;
        this.socRetrievalEnergyMeterValue = 0.0f;
        this.previousSocScriptExecutionResult = null;
        this.chargeLoss = 0.0;
        this.chargingAlmostCompleted = false;
    }

    public void onChargingCompleted() {
        this.socRetrievalForChargingAlmostCompleted = false;
    }

    public Integer getConnectedOrFirstVehicleId() {
        if(this.connectedVehicleId != null) {
            return this.connectedVehicleId;
        }
        if(this.vehicles.size() > 0) {
            return this.vehicles.get(0).getId();
        }
        return null;
    }

    public void setConnectedVehicleId(Integer connectedVehicleId) {
        this.connectedVehicleId = connectedVehicleId;
    }

    public ElectricVehicle getConnectedVehicle() {
        Integer evId = getConnectedOrFirstVehicleId();
        if(evId != null) {
            return getVehicle(evId);
        }
        return null;
    }

    public ElectricVehicle getVehicle(Integer evId) {
        if(evId != null && this.vehicles != null) {
            for(ElectricVehicle electricVehicle : this.vehicles) {
                if(electricVehicle.getId() == evId) {
                    return electricVehicle;
                }
            }
        }
        return null;
    }

    public void setSocValuesChangedListener(SocValuesChangedListener socValuesChangedListener) {
        this.socValuesChangedListener = socValuesChangedListener;
    }

    public SocValues getSocValues() {
        return socValues;
    }

    public Integer getSocInitial() {
        return this.socValues.initial != null ? this.socValues.initial : 0;
    }

    public void setSocInitial(Integer socInitial) {
        this.socValues.initial = socInitial;
    }

    public Integer getSocCurrent() {
        return this.socValues.current != null ? this.socValues.current : 0;
    }

    public void setSocCurrent(Integer socCurrent) {
        this.socValues.current = socCurrent;
    }

    public Long getSocInitialTimestamp() {
        ZoneOffset zoneOffset = ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now());
        return socValues.initialTimestamp != null ? socValues.initialTimestamp.toEpochSecond(zoneOffset) * 1000 : null;
    }

    public void setSocInitialTimestamp(LocalDateTime socInitialTimestamp) {
        if(this.socValues.initialTimestamp == null) {
            this.socValues.initialTimestamp = socInitialTimestamp;
        }
    }

    private void setSocValuesBatteryCapacityFromConnectedVehicle() {
        var ev = getVehicle(this.connectedVehicleId);
        if(ev != null) {
            socValues.batteryCapacity = ev.getBatteryCapacity();
        }
    }

    public void setSocCalculationRequired(boolean socCalculationRequired) {
        this.socCalculationRequired = socCalculationRequired;
    }

    public void updateSoc(LocalDateTime now, Request request, boolean isCharging) {
        boolean socChanged = false;
        if(socValues != null && (this.socCalculationRequired || isCharging)) {
            setSocValuesBatteryCapacityFromConnectedVehicle();
            int calculatedCurrentSoc = calculateCurrentSoc();
            socChanged = this.socValues.current != null && this.socValues.current != calculatedCurrentSoc;
            this.socValues.current = calculatedCurrentSoc;
            if(socChanged) {
                if(request != null) {
                    Integer max = request.getMax(now);
                    if(max < 1000) {
                        chargingAlmostCompleted = true;
                    }
                }
            }
            this.socCalculationRequired = false;
        }

        ElectricVehicle electricVehicle = getConnectedVehicle();
        if(electricVehicle != null && electricVehicle.getSocScript() != null) {
            logger.debug( "{}: SOC retrieval: socCalculationRequired={} socChanged={} chargingAlmostCompleted={} socRetrievalForChargingAlmostCompleted={}",
                    applianceId, socCalculationRequired, socChanged, chargingAlmostCompleted, socRetrievalForChargingAlmostCompleted);

            Integer updateAfterIncrease = electricVehicle.getSocScript().getUpdateAfterIncrease();
            if(updateAfterIncrease == null) {
                updateAfterIncrease = ElectricVehicleChargerDefaults.getUpdateSocAfterIncrease();
            }
            Integer updateAfterSeconds = electricVehicle.getSocScript().getUpdateAfterSeconds();
            if(this.socValues.initial == null
                    || this.socValues.retrieved == null
                    || (chargingAlmostCompleted && !socRetrievalForChargingAlmostCompleted)
                    || ((this.socValues.retrieved + updateAfterIncrease <= this.socValues.current)
                    && (updateAfterSeconds == null || now.minusSeconds(updateAfterSeconds).isAfter(this.socValues.retrievedTimestamp)))
            ) {
                logger.debug( "{}: SOC retrieval is required: {}", applianceId, this.socValues);
                triggerSocScriptExecution(getConnectedOrFirstVehicleId());
            }
            else {
                logger.debug("{}: SOC retrieval is NOT required: {}", applianceId, this.socValues);
            }
        }

        if(socChanged) {
            this.socValuesChangedListener.onSocValuesChanged(this.socValues);
        }
    }

    private int calculateCurrentSoc() {
        ElectricVehicle vehicle = getConnectedVehicle();
        if (vehicle != null) {
            int energyMeteredSinceLastSocScriptExecution = getEnergyMeteredSinceLastSocScriptExecution();
            int socRetrievedOrInitial = this.socValues.retrieved != null ? this.socValues.retrieved : getSocInitial();
            int soc = Long.valueOf(Math.round(
                    socRetrievedOrInitial + energyMeteredSinceLastSocScriptExecution / (vehicle.getBatteryCapacity() *  (1 + chargeLoss/100)) * 100
            )).intValue();
            int socCurrent = Math.min(soc, 100);
            logger.debug("{}: SOC calculation: socCurrent={} socRetrievedOrInitial={} batteryCapacity={}Wh energyMeteredSinceLastSocScriptExecution={}Wh chargeLoss={}",
                    applianceId, percentageFormat.format(socCurrent), percentageFormat.format(socRetrievedOrInitial),
                    vehicle.getBatteryCapacity(),  energyMeteredSinceLastSocScriptExecution, percentageFormat.format(chargeLoss));
            return socCurrent;
        }
        return 0;
    }

    public void triggerSocScriptExecution() {
       this.evIdWithSocScriptExecutor.values().forEach(executor -> executor.triggerExecution(this, socScriptAsync));
    }

    private void triggerSocScriptExecution(Integer evId) {
        var executor = evIdWithSocScriptExecutor.get(evId);
        if(executor != null) {
            executor.triggerExecution(this, socScriptAsync);
        }
    }

    public void terminateSocScriptExecution() {
        this.evIdWithSocScriptExecutor.values().forEach(SocScriptExecutor::terminate);
    }

    @Override
    public void onSocScriptExecutionSuccess(LocalDateTime now, int evId, SocScriptExecutionResult result) {
        logger.debug("{}: SOC script execution success: result={} previousSocScriptExecutionResult={}",
                applianceId, result, previousSocScriptExecutionResult);

        var betterPluginStatus = isBetterPluginStatus(result, previousSocScriptExecutionResult);
        var pluginTimeJustNow = isPluginTimeJustNow(now, getPluginTime(result.pluginTime));
        var matchingLocation = isMatchingLocation(result.location);
        logger.debug("{}: evId={} betterPluginStatus={} pluginTimeJustNow={} matchingLocation={}",
                applianceId, evId, betterPluginStatus, pluginTimeJustNow, matchingLocation);

        if(previousSocScriptExecutionResult == null || (
               betterPluginStatus
            || pluginTimeJustNow
            || matchingLocation
        )) {
            if(this.connectedVehicleId == null) {
                logger.debug("{}: Identified connected vehicle: id={}", applianceId, evId);
            }
            else if(this.connectedVehicleId != evId) {
                logger.debug("{}: Changing connected vehicle to: id={}", applianceId, evId);
            }
            this.connectedVehicleId = evId;

            var chargeLoss = getVehicle(evId).getChargeLoss();
            this.chargeLoss = chargeLoss != null ? chargeLoss.floatValue() : 0.0f;
        }
        if(this.connectedVehicleId != null) {
            handleSocScriptExecutionResult(now, evId, result);
        }
        this.previousSocScriptExecutionResult = result;
    }

    @Override
    public void onSocScriptExecutionFailure(LocalDateTime now) {
        logger.warn("{}: SOC script execution failed", applianceId);
        this.connectedVehicleId = getConnectedOrFirstVehicleId();
        handleSocScriptExecutionResult(now, this.connectedVehicleId, new SocScriptExecutionResult(
                socValues.initial == null ? 0: socValues.current, null, null, null));
    }

    private void handleSocScriptExecutionResult(LocalDateTime now, int evId, SocScriptExecutionResult result) {
        if(this.connectedVehicleId != evId) {
            logger.debug("{}: Ignore SOC script execution result for vehicle: id={}", applianceId, evId);
            return;
        }
        setSocValuesBatteryCapacityFromConnectedVehicle();
        Integer socLastRetrieved = socValues.retrieved != null ? socValues.retrieved : socValues.initial;
        if(socValues.initial == null) {
            socValues.initial = result.soc.intValue();
            socValues.initialTimestamp = now;
            socValues.current = result.soc.intValue();
        }
        socValues.retrieved = result.soc.intValue();
        if(socLastRetrieved != null) {
            Double chargeLossCalculated = calculateChargeLoss(getEnergyMeteredSinceLastSocScriptExecution(), socValues.retrieved, socLastRetrieved);
            if(chargeLossCalculated != null) {
                if(chargeLossCalculated > 50) {
                    chargeLoss = 50.0;
                    logger.debug("{}: Ignoring calculated charge loss. Limit charge loss to {}", applianceId, chargeLoss);
                }
                else if(chargeLossCalculated < 0) {
                    chargeLoss = getVehicle(evId).getChargeLoss();
                    logger.debug("{}: Ignoring calculated charge loss. Using vehicle default value of {}", applianceId, chargeLoss);
                }
                else {
                    chargeLoss = chargeLossCalculated;
                }
            }
        }
        socValues.current = result.soc.intValue();
        socRetrievalEnergyMeterValue = meterMessage != null ? meterMessage.energy : 0.0;
        if(this.chargingAlmostCompleted) {
            socRetrievalForChargingAlmostCompleted = true;
        }
        this.socValuesChangedListener.onSocValuesChanged(this.socValues);
    }

    protected boolean isBetterPluginStatus(SocScriptExecutionResult result, SocScriptExecutionResult previousResult) {
        return result.pluggedIn != null && result.pluggedIn && (previousResult == null || previousResult.pluggedIn == null || !previousResult.pluggedIn);
    }

    protected boolean isPluginTimeJustNow(LocalDateTime now, LocalDateTime pluginTime) {
        if(pluginTime == null) {
            return false;
        }
        return pluginTime.plusMinutes(5).isAfter(now);
    }

    protected LocalDateTime getPluginTime(String pluginTime) {
        if(pluginTime == null) {
            return null;
        }
        var fields = pluginTime.split(":");
        return LocalDateTime.now().withHour(Integer.parseInt(fields[0])).withMinute(Integer.parseInt(fields[1]));
    }

    protected boolean isMatchingLocation(Pair<Double, Double> evLocation) {
        if(this.evChargerLocation == null || evLocation == null) {
            return false;
        }
        var meters = SloppyMath.haversinMeters(evChargerLocation.getKey(), evChargerLocation.getValue(), evLocation.getKey(), evLocation.getValue());
        return meters < 100;
    }

    private int getEnergyMeteredSinceLastSocScriptExecution() {
        int energyMeteredSinceLastSocScriptExecution = 0; // in Wh
        Double energyMetered = meterMessage != null ? meterMessage.energy : 0.0;
        energyMeteredSinceLastSocScriptExecution = Double.valueOf((energyMetered - socRetrievalEnergyMeterValue) * 1000.0f).intValue();
        logger.trace("{}: Calculate energyMeteredSinceLastSocScriptExecution={} meterMessage={} energyMetered={} socRetrievalEnergyMeterValue={}",
                applianceId, energyMeteredSinceLastSocScriptExecution, meterMessage != null, energyMetered, socRetrievalEnergyMeterValue);
        return energyMeteredSinceLastSocScriptExecution;
    }

    public double getChargeLoss() {
        return chargeLoss;
    }

    public Double calculateChargeLoss(int energyMeteredSinceLastSocScriptExecution, int socCurrent, int socLastRetrieval) {
        var evId = getConnectedOrFirstVehicleId();
        var batteryCapacity = evId != null ? getVehicle(evId).getBatteryCapacity() : null;
        if (batteryCapacity != null && energyMeteredSinceLastSocScriptExecution > 0) {
            double energyReceivedByEv = (socCurrent - socLastRetrieval)/100.0 * batteryCapacity;
            double chargeLoss = energyMeteredSinceLastSocScriptExecution * 100.0 / energyReceivedByEv - 100.0;
            logger.debug("{}: charge loss calculation: chargeLoss={} socCurrent={} socLastRetrieval={} batteryCapacity={}Wh energyMeteredSinceLastSocScriptExecution={}Wh energyReceivedByEv={}Wh",
                    applianceId, percentageFormat.format(chargeLoss), percentageFormat.format(socCurrent), percentageFormat.format(socLastRetrieval),
                    batteryCapacity, energyMeteredSinceLastSocScriptExecution, (int) energyReceivedByEv);
            return chargeLoss;
        }
        return null;
    }
}
