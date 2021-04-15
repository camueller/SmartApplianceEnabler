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

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SocScriptTest {

    private SocScript socScript = new SocScript();

    @Test
    public void getStateOfCharge() throws Exception {
        File socScriptFile = writeSocScriptFile("#!/bin/sh\necho \"Car SOC is 42.7 percent.\"\n");
        socScript.setScript(socScriptFile.getAbsolutePath());
        socScript.setExtractionRegex(".*is (\\d*.{0,1}\\d+).*");
        assertEquals(42.7f, socScript.getStateOfCharge(), 0.01f);
    }

    @Test
    public void getStateOfCharge_negativeReturnCode() throws Exception {
        File socScriptFile = writeSocScriptFile("#!/bin/sh\necho \"Car SOC is 42.7 percent.\"\nexit 1\\n");
        socScript.setScript(socScriptFile.getAbsolutePath());
        socScript.setExtractionRegex(".*is (\\d*.{0,1}\\d+).*");
        assertNull(socScript.getStateOfCharge());
    }

    private File writeSocScriptFile(String content) throws Exception {
        File socScriptFile = File.createTempFile("soc", ".sh");
        socScriptFile.setExecutable(true);
        PrintWriter out = new PrintWriter(socScriptFile);
        out.print(content);
        out.close();
        return socScriptFile;
    }
}
