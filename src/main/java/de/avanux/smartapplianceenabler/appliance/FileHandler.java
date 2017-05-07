/*
 * Copyright (C) 2017 Axel Müller <axel.mueller@avanux.de>
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

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class FileHandler {

    private static final String SAE_HOME = "sae.home";
    private static String FILE_DIR;
    private static Logger logger = LoggerFactory.getLogger(FileHandler.class);
    private static final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");

    static {
        FILE_DIR=System.getProperty(SAE_HOME);
        if(FILE_DIR == null) {
            logger.error("Property " + SAE_HOME + " not set.");
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

    public void saveHolidays(Map<LocalDate, String> holidayWithName) {
        File holidayFile = getHolidayFile();
        try {
            Writer writer = new FileWriter(holidayFile);
            for(LocalDate date : holidayWithName.keySet()) {
                String dateString = dateFormatter.print(date);
                String name = holidayWithName.get(date);
                writer.write(dateString + " " + name + "\n");
            }
            writer.close();
            logger.debug(holidayWithName.size() + " holidays written to " + holidayFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Error writing holidays file " + holidayFile.getAbsolutePath(), e);
        }
    }

    public List<LocalDate> loadHolidays() {
        File holidayFile = getHolidayFile();
        if(holidayFile.exists()) {
            logger.debug("Using holidays file " + holidayFile.getAbsolutePath());
            List<LocalDate> holidays = new ArrayList<>();
            try {
                List<String> lines = Files.readAllLines(holidayFile.toPath(), StandardCharsets.UTF_8);
                for(String line : lines) {
                    final String[] lineSegments = line.split("\\s+");
                    LocalDate holiday = dateFormatter.parseLocalDate(lineSegments[0]);
                    holidays.add(holiday);
                    logger.debug("Loaded holiday: " + holiday);
                }
            } catch (IOException e) {
                logger.debug("Error reading holidays file " + holidayFile.getAbsolutePath(), e);
            }
            return holidays;
        }
        return null;
    }

    public boolean isHolidayFileAvailable() {
        return getHolidayFile().exists();
    }

    private File getHolidayFile() {
        int year = new LocalDate().getYear();
        return new File(FILE_DIR, "Holidays-" + year + ".txt");
    }
}
