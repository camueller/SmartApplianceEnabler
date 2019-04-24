/*
 * Copyright (C) 2019 Axel Müller <axel.mueller@avanux.de>
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

package de.avanux.smartapplianceenabler.control.ev.http;

import de.avanux.smartapplianceenabler.http.HttpTransactionExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class HttpRead extends HttpTransactionExecutor {
    private transient Logger logger = LoggerFactory.getLogger(HttpRead.class);
    @XmlAttribute
    private String url;
    @XmlElement(name = "HttpReadValue")
    private List<HttpReadValue> readValues;

    public HttpRead() {
    }

    public HttpRead(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public List<HttpReadValue> getReadValues() {
        return readValues;
    }

    public void setReadValues(List<HttpReadValue> readValues) {
        this.readValues = readValues;
    }
}
