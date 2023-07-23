/*
 * Copyright (C) 2018 Axel Müller <axel.mueller@avanux.de>
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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.io.*;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@XmlAccessorType(XmlAccessType.FIELD)
public class SocScript implements ApplianceIdConsumer {

    private transient Logger logger = LoggerFactory.getLogger(SocScript.class);
    @XmlAttribute
    private String script;
    @XmlAttribute
    private Integer updateAfterIncrease;
    @XmlAttribute
    private Integer updateAfterSeconds;
    @XmlAttribute
    private Integer timeoutSeconds;
    @XmlAttribute
    private String extractionRegex;
    @XmlAttribute
    private String pluginStatusExtractionRegex;
    @XmlAttribute
    private String pluginTimeExtractionRegex;
    @XmlAttribute
    private String latitudeExtractionRegex;
    @XmlAttribute
    private String longitudeExtractionRegex;
    private transient String applianceId;


    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public Integer getUpdateAfterIncrease() {
        return updateAfterIncrease;
    }

    public Integer getUpdateAfterSeconds() {
        return updateAfterSeconds;
    }

    public Integer getTimeoutSeconds() {
        return timeoutSeconds != null ? timeoutSeconds : ElectricVehicleChargerDefaults.getSocScriptTimeoutSeconds();
    }

    public String getExtractionRegex() {
        return extractionRegex;
    }

    public void setExtractionRegex(String extractionRegex) {
        this.extractionRegex = extractionRegex;
    }

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    public SocScriptExecutionResult getResult() {
        if(this.script != null) {
            File scriptFile = new File(this.script);
            if(scriptFile.exists()) {
                if(scriptFile.canExecute()) {
                    String output = getScriptOutput(this.script);
                    if(output != null && output.length() > 0) {
                        var result = new SocScriptExecutionResult();

                        try {
                            String socValueString = extractValue(output, this.extractionRegex);
                            if(socValueString != null) {
                                result.soc = parseDouble(socValueString).intValue();
                            }

                            if(this.pluginStatusExtractionRegex != null) {
                                result.pluggedIn = matchValue(output, this.pluginStatusExtractionRegex);
                            }

                            if(this.pluginTimeExtractionRegex != null) {
                                result.pluginTime = extractValue(output, this.pluginTimeExtractionRegex);
                            }

                            if(this.latitudeExtractionRegex != null && this.longitudeExtractionRegex != null) {
                                String latitudeValueString = extractValue(output, this.latitudeExtractionRegex);
                                String longitudeValueString = extractValue(output, this.longitudeExtractionRegex);
                                if(latitudeValueString != null && longitudeValueString != null) {
                                    result.location = new ImmutablePair<>(
                                            parseDouble(latitudeValueString),
                                            parseDouble(longitudeValueString)
                                    );
                                }
                            }
                        }
                        catch(Exception e) {
                            logger.error("{}: Error parsing SoC script output", applianceId, e);
                        }

                        return result;
                    }
                }
                else {
                    logger.error("{}: SoC script file is not executable: {}", applianceId, this.script);
                }
            }
            else {
                logger.error("{}: SoC script file not found: {}", applianceId, this.script);
            }
        }
        else {
            logger.warn("{}: No SoC script configured.", applianceId);
        }
        return null;
    }

    private Double parseDouble(String input) {
        return Double.parseDouble(input.replace(',', '.'));
    }

    private String getScriptOutput(String scriptToExecute) {
        InputStream inputStream = null;
        try {
            logger.debug("{}: Executing SoC script: {}", applianceId, scriptToExecute);
            ProcessBuilder builder = new ProcessBuilder(scriptToExecute);
            builder.redirectErrorStream(true);
            Process p = builder.start();
            StringBuffer scriptOutput = new StringBuffer();
            inputStream = p.getInputStream();
            int c;
            while ((c = inputStream.read()) != -1) {
                scriptOutput.append((char) c);
            }
            logger.debug("{}: SoC script output: {}", applianceId, scriptOutput);
            int rc = p.waitFor();
            logger.debug("{}: SoC script exited with return code {}", applianceId, rc);
            if(rc == 0) {
                return scriptOutput.toString();
            }
        } catch (Exception e) {
            logger.error("{}: Error executing SoC script {}", applianceId, scriptToExecute, e);
        } finally {
            try {
                Objects.requireNonNull(inputStream).close();
            } catch (IOException e) {
                logger.error("{}: Error closing input stream of SoC script {}", applianceId, scriptToExecute, e);
            }
        }
        return null;
    }

    /**
     * Extract the value from the the response using a regular expression.
     * The regular expression has contain a capture group containing the value.
     * @param text the text containing the value
     * @param regex the regular expression to be used to extract the value
     * @return the value extracted or the full text if the regular expression is null or could not be matched
     */
    protected String extractValue(String text, String regex)  {
        if(regex == null || regex.length() == 0) {
            return text;
        }
        logger.debug("{}: Value extraction regex: {}", applianceId, regex);
        Matcher regexMatcher = Pattern.compile(regex, Pattern.DOTALL).matcher(text.trim());
        if (regexMatcher.find()) {
            return regexMatcher.group(1);
        }
        return text;
    }

    protected boolean matchValue(String text, String regex) {
        if(regex == null || regex.length() == 0) {
            return false;
        }
        logger.debug("{}: Value match regex: {}", applianceId, regex);
        Matcher regexMatcher = Pattern.compile(regex, Pattern.DOTALL).matcher(text.trim());
        return regexMatcher.matches();
    }

    @Override
    public String toString() {
        return "SocScript{" +
                "script='" + script + '\'' +
                ", updateAfterIncrease=" + updateAfterIncrease +
                ", updateAfterSeconds=" + updateAfterSeconds +
                ", timeoutSeconds=" + timeoutSeconds +
                ", extractionRegex='" + extractionRegex + '\'' +
                ", pluginStatusExtractionRegex='" + pluginStatusExtractionRegex + '\'' +
                ", pluginTimeExtractionRegex='" + pluginTimeExtractionRegex + '\'' +
                ", latitudeExtractionRegex='" + latitudeExtractionRegex + '\'' +
                ", longitudeExtractionRegex='" + longitudeExtractionRegex + '\'' +
                '}';
    }
}
