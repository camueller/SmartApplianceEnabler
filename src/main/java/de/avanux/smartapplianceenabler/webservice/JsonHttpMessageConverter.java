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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.jayway.jsonpath.DocumentContext;
import de.avanux.smartapplianceenabler.control.Control;
import de.avanux.smartapplianceenabler.control.ev.EVChargerControl;
import de.avanux.smartapplianceenabler.meter.Meter;
import de.avanux.smartapplianceenabler.schedule.Request;
import de.avanux.smartapplianceenabler.schedule.Timeframe;
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
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Converter that can convert HTTP to JSON.
 * FIXME: Rename file
 */
public class JsonHttpMessageConverter implements HttpMessageConverter {

    private Logger logger = LoggerFactory.getLogger(JsonHttpMessageConverter.class);
    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    private List<MediaType> supportedMediaTypes = new ArrayList<>();
    private Gson gson;
    private DocumentContext context;

    public JsonHttpMessageConverter() {
        this.supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        this.supportedMediaTypes.add(new MediaType("application", "*+json", DEFAULT_CHARSET));

        this.gson = new GsonBuilder()
                .registerTypeAdapter(Meter.class, new MeterTypeAdapter())
                .registerTypeAdapter(Control.class, new ControlTypeAdapter())
                .registerTypeAdapter(EVChargerControl.class, new EVChargerControlTypeAdapter())
                .registerTypeAdapter(Request.class, new RequestTypeAdapter())
                .registerTypeAdapter(Timeframe.class, new TimeframeTypeAdapter())
                .registerTypeAdapterFactory(numericStringCoercingFactory())
                .setPrettyPrinting()
                .create();
    }

    @Override
    public boolean canRead(Class aClass, MediaType mediaType) {
        return mediaType == null || supportedMediaTypes.stream().anyMatch(mt -> mt.isCompatibleWith(mediaType));
    }

    @Override
    public boolean canWrite(Class aClass, MediaType mediaType) {
        return mediaType == null || supportedMediaTypes.stream().anyMatch(mt -> mt.isCompatibleWith(mediaType));
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return this.supportedMediaTypes;
    }

    @Override
    public Object read(Class toType, HttpInputMessage httpInputMessage) throws IOException, HttpMessageNotReadableException {
        String body = StreamUtils.copyToString(httpInputMessage.getBody(), Charset.defaultCharset());
        logger.trace("Deserializing JSON object to " + toType.getName());
        return gson.fromJson(new StringReader(body), toType);
    }

    @Override
    public void write(Object object, MediaType mediaType, HttpOutputMessage httpOutputMessage) throws IOException, HttpMessageNotWritableException {
        logger.trace("Serializing " + object.getClass() + " to JSON");
        httpOutputMessage.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        Writer writer = new StringWriter();

        JsonElement element = this.gson.toJsonTree(object);
        if(element.isJsonObject()) {
            element.getAsJsonObject().add("@class", new JsonPrimitive(object.getClass().getName()));
        }
        this.gson.toJson(element, writer);

        httpOutputMessage.getBody().write(writer.toString().getBytes(DEFAULT_CHARSET));
    }

    /**
     * Returns a TypeAdapterFactory that coerces JSON strings to numeric types.
     * Angular text inputs always produce strings, but Java fields may be int/long/float/double.
     */
    private TypeAdapterFactory numericStringCoercingFactory() {
        return new TypeAdapterFactory() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> com.google.gson.TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                Class<T> raw = (Class<T>) type.getRawType();
                if (raw != int.class && raw != Integer.class
                        && raw != long.class && raw != Long.class
                        && raw != float.class && raw != Float.class
                        && raw != double.class && raw != Double.class) {
                    return null;
                }
                com.google.gson.TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
                return new com.google.gson.TypeAdapter<T>() {
                    @Override
                    public void write(JsonWriter out, T value) throws IOException {
                        delegate.write(out, value);
                    }
                    @Override
                    public T read(JsonReader in) throws IOException {
                        if (in.peek() == JsonToken.STRING) {
                            String s = in.nextString();
                            if (s == null || s.isEmpty()) return null;
                            if (raw == int.class || raw == Integer.class)
                                return (T) Integer.valueOf((int) Double.parseDouble(s));
                            if (raw == long.class || raw == Long.class)
                                return (T) Long.valueOf((long) Double.parseDouble(s));
                            if (raw == float.class || raw == Float.class)
                                return (T) Float.valueOf(s);
                            if (raw == double.class || raw == Double.class)
                                return (T) Double.valueOf(s);
                        }
                        return delegate.read(in);
                    }
                };
            }
        };
    }
}
