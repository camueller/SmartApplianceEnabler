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

import de.avanux.smartapplianceenabler.util.Downloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvChargerTemplatesDownloader extends Downloader {

    private Logger logger = LoggerFactory.getLogger(EvChargerTemplatesDownloader.class);

    public String download() {
        var url = "https://raw.githubusercontent.com/camueller/SmartApplianceEnabler/master/run/evcharger-templates.json";
        logger.debug("Downloading ev charger templates from {}", url);
        return downloadAsString(url);
    }
}
