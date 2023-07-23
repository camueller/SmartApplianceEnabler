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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class SocScriptExecutor implements Runnable, ApplianceIdConsumer {
    private transient Logger logger = LoggerFactory.getLogger(SocScriptExecutor.class);
    private transient String applianceId;

    private Thread thread;
    private LocalDateTime nowForTesting;
    private LocalDateTime triggeredAt;
    private SocScript socScript;
    private int evId;
    private SocScriptExecutionResultListener listener;


    public SocScriptExecutor(int evId, SocScript socScript) {
        this.evId = evId;
        this.socScript = socScript;
    }

    @Override
    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
        if(socScript != null) {
            this.socScript.setApplianceId(applianceId);
        }
    }

    public void setNowForTesting(LocalDateTime nowForTesting) {
        this.nowForTesting = nowForTesting;
    }

    public void triggerExecution(SocScriptExecutionResultListener listener, boolean socScriptAsync) {
        if(this.socScript == null) {
            logger.debug( "{}: No SOC script configured: evId={}", applianceId, evId);
            return;
        }
        this.listener = listener;
        if(this.thread == null) {
            logger.debug( "{}: Trigger SOC retrieval: evId={}", applianceId, evId);
            if(socScriptAsync) {
                this.thread = new Thread(this);
                this.thread.start();
            }
            else {
                // for unit tests
                run();
            }
            this.triggeredAt = LocalDateTime.now();
        }
        else {
            if(this.triggeredAt != null && LocalDateTime.now().minusSeconds(socScript.getTimeoutSeconds()).isAfter(this.triggeredAt)) {
                logger.warn("{}: SOC script timeout: evId={}", applianceId, evId);
                terminate();
                reportSocScriptFailure();
            }
            else {
                logger.debug("{}: SOC retrieval already running: evId={}", applianceId, evId);
            }
        }
    }

    public void terminate() {
        if(this.thread != null) {
            logger.warn("{}: interrupting SOC script: evId={}", applianceId, evId);
            this.thread.interrupt();
        }
        setThread(null);
    }

    private void setThread(Thread thread) {
        this.thread = thread;
    }

    @Override
    public void run() {
        if(socScript.getScript() != null) {
            var result = socScript.getResult();
            logger.debug("{}: SOC script execution result: {}", applianceId, result);
            if(result != null) {
                this.listener.onSocScriptExecutionSuccess(
                        this.nowForTesting != null ? this.nowForTesting : LocalDateTime.now(), this.evId, result);
            }
            else {
                reportSocScriptFailure();
            }
        }
        setThread(null);
    }

    private void reportSocScriptFailure() {
        this.listener.onSocScriptExecutionFailure(
                this.nowForTesting != null ? this.nowForTesting : LocalDateTime.now());
    }
}
