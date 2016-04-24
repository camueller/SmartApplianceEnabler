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

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.servlet.Servlet;

import org.fourthline.cling.DefaultUpnpServiceConfiguration;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.binding.LocalServiceBindingException;
import org.fourthline.cling.binding.xml.DeviceDescriptorBinder;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.transport.impl.apache.StreamClientConfigurationImpl;
import org.fourthline.cling.transport.impl.apache.StreamServerConfigurationImpl;
import org.fourthline.cling.transport.spi.NetworkAddressFactory;
import org.fourthline.cling.transport.spi.ServletContainerAdapter;
import org.fourthline.cling.transport.spi.StreamClient;
import org.fourthline.cling.transport.spi.StreamServer;

import de.avanux.smartapplianceenabler.SmartApplianceEnabler;

public class SempDiscovery implements Runnable {

    public void run() {
        try {
            final ExecutorService executorService = Executors.newSingleThreadExecutor();
            
            final UpnpService upnpService = new UpnpServiceImpl(new DefaultUpnpServiceConfiguration() {
                @Override
                public DeviceDescriptorBinder getDeviceDescriptorBinderUDA10() {
                    return new SempDeviceDescriptorBinderImpl(this);
                }
                
                @Override
                public StreamClient createStreamClient() {
                    // disable the client in order to avoid requesting descriptors from UPnP devices
                    return null;
                }
                
                @Override
                public StreamServer createStreamServer(NetworkAddressFactory networkAddressFactory) {
                    return new org.fourthline.cling.transport.impl.apache.StreamServerImpl(
                            new StreamServerConfigurationImpl()
                            );
                }
            });

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    upnpService.shutdown();
                }
            });

            // Add the bound local device to the registry
            upnpService.getRegistry().addDevice(createDevice());

        }
        catch (Exception ex) {
            System.err.println("Exception occured: " + ex);
            ex.printStackTrace(System.err);
            System.exit(1);
        }
    }
    
    
    private LocalDevice createDevice()
            throws ValidationException, LocalServiceBindingException, IOException {

        DeviceIdentity identity =
                new DeviceIdentity(
                        UDN.uniqueSystemIdentifier(SmartApplianceEnabler.class.getSimpleName())
                );

        DeviceType type = new SmartApplianceEnablerDeviceType();

        DeviceDetails details =
                new DeviceDetails(
                        SmartApplianceEnabler.class.getSimpleName(),
                        new ManufacturerDetails(SmartApplianceEnabler.MANUFACTURER_NAME, URI.create(SmartApplianceEnabler.MANUFACTURER_URI)),
                        new ModelDetails(
                                SmartApplianceEnabler.class.getSimpleName(),
                                SmartApplianceEnabler.DESCRIPTION,
                                SmartApplianceEnabler.VERSION,
                                URI.create(SmartApplianceEnabler.MODEL_URI)
                        )
                );

        return new LocalDevice(identity, type, details, (Icon) null, (LocalService) null);
    }    
}
