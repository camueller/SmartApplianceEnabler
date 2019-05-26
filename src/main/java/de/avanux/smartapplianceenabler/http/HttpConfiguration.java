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

import org.apache.http.entity.ContentType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class HttpConfiguration {
    @XmlAttribute
    private String contentType;
    @XmlAttribute
    private String username;
    @XmlAttribute
    private String password;

    public HttpConfiguration() {
    }

    public HttpConfiguration(String contentType, String username, String password) {
        this.contentType = contentType;
        this.username = username;
        this.password = password;
    }

    protected ContentType getContentType() {
        if(contentType != null) {
            return ContentType.create(contentType);
        }
        return null;
    }
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
