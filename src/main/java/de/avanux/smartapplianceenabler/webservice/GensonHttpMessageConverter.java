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

package de.avanux.smartapplianceenabler.webservice;

import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import com.owlike.genson.reflect.VisibilityFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Converter that can convert HTTP to JSON using Genson.
 */
public class GensonHttpMessageConverter implements HttpMessageConverter {

    private Logger logger = LoggerFactory.getLogger(GensonHttpMessageConverter.class);
    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    private List<MediaType> supportedMediaTypes = new ArrayList<>();
    private Genson genson;

    public GensonHttpMessageConverter() {
        this.supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        this.supportedMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        this.supportedMediaTypes.add(new MediaType("application", "*+json", DEFAULT_CHARSET));

        this.genson = new GensonBuilder()
                .useFields(true, VisibilityFilter.PRIVATE)
                .useMethods(false)
                .useClassMetadata(true)
                .useRuntimeType(true)
                .create();
    }

    @Override
    public boolean canRead(Class aClass, MediaType mediaType) {
        return supportedMediaTypes.contains(mediaType);
    }

    @Override
    public boolean canWrite(Class aClass, MediaType mediaType) {
        return supportedMediaTypes.contains(mediaType);
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return this.supportedMediaTypes;
    }

    @Override
    public Object read(Class toType, HttpInputMessage httpInputMessage) throws IOException, HttpMessageNotReadableException {
        logger.debug("Deserializing JSON to " + toType);
        String body = StreamUtils.copyToString(httpInputMessage.getBody(), Charset.defaultCharset());
        return this.genson.deserialize(body, toType);
    }

    @Override
    public void write(Object object, MediaType mediaType, HttpOutputMessage httpOutputMessage) throws IOException, HttpMessageNotWritableException {
        logger.debug("Serializing " + object.getClass() + " to JSON");
        String json = this.genson.serialize(object);
        httpOutputMessage.getBody().write(json.getBytes());
    }
}
