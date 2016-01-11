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
package de.avanux.smartapplianceenabler.appliance;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FileHandler {

    private static String FILE_DIR;
    private static Logger logger = LoggerFactory.getLogger(FileHandler.class); 
    
    static {
        FILE_DIR=System.getProperty("sae.home");
        if(FILE_DIR == null) {
            logger.error("Property appliance.dir not set.");
            System.exit(-1);
        }
        logger.info("Using appliance directory " + FILE_DIR);
    }

    public <R extends Object> R load(Class<R> rootElementType) {
        File file = getFile(rootElementType);
        try {
            JAXBContext context = JAXBContext.newInstance(rootElementType);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (R) unmarshaller.unmarshal(file);
        }
        catch(JAXBException e) {
            logger.error("Error unmarshalling file " + file, e);
        }
        return null;
    }
    
    private File getFile(Class<? extends Object> rootElementType) {
        return new File(FILE_DIR + System.getProperty("file.separator") + rootElementType.getSimpleName() + ".xml");
    }    
    
}
