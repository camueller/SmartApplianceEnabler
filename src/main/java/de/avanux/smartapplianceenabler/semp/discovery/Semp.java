/*
 * Copyright (C) 2015 Axel Müller <axel.mueller@avanux.de>
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
package de.avanux.smartapplianceenabler.semp.discovery;

import org.w3c.dom.Node;

public class Semp {

    public static final String NAMESPACE = "schemas-simple-energy-management-protocol";    
    public static final String PREFIX = "semp";    
    public static final String XSD_VERSION = "1.1.5";
    
    public enum ELEMENT {
        X_SEMPSERVICE,
        server, 
        basePath,
        transport,
        exchangeFormat,
        wsVersion
        ;

        public static ELEMENT valueOrNullOf(String s) {
            try {
                return valueOf(s);
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }

        public boolean equals(Node node) {
            return toString().equals(node.getLocalName());
        }
    }
    
    public static String prefixed(ELEMENT element)  {
        return Semp.PREFIX + ":" + element;
    }
}
