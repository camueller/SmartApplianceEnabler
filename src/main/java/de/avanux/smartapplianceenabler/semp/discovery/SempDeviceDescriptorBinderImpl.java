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

import static org.fourthline.cling.model.XMLUtil.appendNewElementIfNotNull;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;

import org.fourthline.cling.DefaultUpnpServiceConfiguration;
import org.fourthline.cling.binding.xml.UDA10DeviceDescriptorBinderImpl;
import org.fourthline.cling.model.Namespace;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.profile.RemoteClientInfo;
import org.fourthline.cling.transport.spi.NetworkAddressFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.avanux.smartapplianceenabler.semp.discovery.Semp.ELEMENT;

public class SempDeviceDescriptorBinderImpl extends UDA10DeviceDescriptorBinderImpl {

    private Logger logger = LoggerFactory.getLogger(SempDeviceDescriptorBinderImpl.class);
    private DefaultUpnpServiceConfiguration serviceConfiguration;
    private String sempServerUrl;
    
    public SempDeviceDescriptorBinderImpl(DefaultUpnpServiceConfiguration serviceConfiguration, String sempServerUrl) {
        this.serviceConfiguration = serviceConfiguration;
        this.sempServerUrl = sempServerUrl;
    }

    @Override
    protected void generateDevice(Namespace namespace, Device deviceModel, Document descriptor, Element rootElement,
            RemoteClientInfo info) {
        super.generateDevice(namespace, deviceModel, descriptor, rootElement, info);
        
        
        NodeList deviceElements = rootElement.getElementsByTagName(org.fourthline.cling.binding.xml.Descriptor.Device.ELEMENT.device.toString());
        if(deviceElements.getLength() > 0) {
            Element deviceElement = (Element) deviceElements.item(0);
            
            Element sempServiceElement = appendNewElementIfNotNull(
                    descriptor, deviceElement, Semp.prefixed(ELEMENT.X_SEMPSERVICE),
                    "", "urn:" + Semp.NAMESPACE + ":service-1-0"
            );
            
            appendNewElementIfNotNull(descriptor, sempServiceElement, Semp.prefixed(ELEMENT.server), sempServerUrl);
            appendNewElementIfNotNull(descriptor, sempServiceElement, Semp.prefixed(ELEMENT.basePath), "/semp");
            appendNewElementIfNotNull(descriptor, sempServiceElement, Semp.prefixed(ELEMENT.transport), "HTTP/Pull");
            appendNewElementIfNotNull(descriptor, sempServiceElement, Semp.prefixed(ELEMENT.exchangeFormat), "XML");
            appendNewElementIfNotNull(descriptor, sempServiceElement, Semp.prefixed(ELEMENT.wsVersion), Semp.XSD_VERSION);
        }
    }
}
