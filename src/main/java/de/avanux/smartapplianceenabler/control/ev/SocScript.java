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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
    private String extractionRegex;
    private transient Pattern socValueExtractionPattern;
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

    public Double getStateOfCharge() {
        if(this.script != null) {
            String output = getScriptOutput(this.script);
            if(output != null && output.length() > 0) {
                String socValueString = extractSoCValue(output, this.extractionRegex);
                Double soc = Double.parseDouble(socValueString.replace(',', '.'));
                logger.debug("{}: SoC: {}", applianceId, soc);
                return soc;
            }
        }
        else {
            logger.warn("{}: No SoC script configured.", applianceId);
        }
        return null;
    }

    private String getScriptOutput(String scriptToExecute) {
        try {
            logger.debug("{}: Executing SoC script: {}", applianceId, scriptToExecute);
            ProcessBuilder builder = new ProcessBuilder(scriptToExecute);
            builder.redirectErrorStream(true);
            Process p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuffer scriptOutput = new StringBuffer();
            while (true) {
                String line = r.readLine();
                if (line == null) {
                    break;
                }
                scriptOutput = scriptOutput.append(line);
            }
            logger.debug("{}: SoC script output: {}", applianceId, scriptOutput.toString());
            int rc = p.exitValue();
            logger.debug("{}: SoC script exited with return code {}", applianceId, rc);
            if(rc == 0) {
                return scriptOutput.toString();
            }
        } catch (IOException e) {
            logger.error("{}: Error executing SoC script {}", applianceId, scriptToExecute, e);
        }
        return null;
    }

    /**
     * Extract the SoC value from the the response using a regular expression.
     * The regular expression has contain a capture group containing the SOC value.
     * @param text the text containing the SOC value
     * @param regex the regular expression to be used to extract the SOC value
     * @return the SOC value extracted or the full text if the regular expression is null or could not be matched
     */
    protected String extractSoCValue(String text, String regex)  {
        if(regex == null) {
            return text;
        }
        logger.debug("{}: SoC extraction regex: {}", applianceId, regex);
        if( this.socValueExtractionPattern == null) {
            this.socValueExtractionPattern = Pattern.compile(regex, Pattern.DOTALL);
        }
        Matcher regexMatcher = this.socValueExtractionPattern.matcher(text);
        if (regexMatcher.find()) {
            return regexMatcher.group(1);
        }
        return text;
    }

    @Override
    public String toString() {
        return "SocScript{" +
                "script='" + script + '\'' +
                ", updateAfterIncrease=" + updateAfterIncrease +
                ", updateAfterSeconds=" + updateAfterSeconds +
                ", extractionRegex='" + extractionRegex + '\'' +
                '}';
    }
}
