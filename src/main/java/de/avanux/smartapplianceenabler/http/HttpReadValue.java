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

package de.avanux.smartapplianceenabler.http;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class HttpReadValue {
    @XmlAttribute
    private String name;
    @XmlAttribute
    private String method;
    @XmlAttribute
    private String data;
    @XmlAttribute
    private String path;
    @XmlAttribute
    private String extractionRegex;
    @XmlAttribute
    private Double factorToValue;

    public HttpReadValue() {
    }

    public HttpReadValue(String name, String path, String extractionRegex) {
        this(name, path, null, extractionRegex,null);
    }

    public HttpReadValue(String name, String path, String data, String extractionRegex, Double factorToValue) {
        this.name = name;
        this.path = path;
        this.data = data;
        this.extractionRegex = extractionRegex;
        this.factorToValue = factorToValue;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public HttpMethod getMethod() {
        return method != null ? HttpMethod.valueOf(method) : null;
    }

    public String getData() {
        return data;
    }

    public String getExtractionRegex() {
        return extractionRegex;
    }

    public Double getFactorToValue() {
        return factorToValue;
    }
}
