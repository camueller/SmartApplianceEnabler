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

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
//import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
//import org.springframework.boot.autoconfigure.web.ResourceProperties;
//import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
//import org.springframework.boot.autoconfigure.web.WebMvcProperties;
//import org.springframework.boot.context.properties.EnableConfigurationProperties;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Import;
//import org.springframework.core.io.Resource;
//import org.springframework.http.converter.HttpMessageConverter;
//import org.springframework.web.servlet.DispatcherServlet;
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
//import org.springframework.web.servlet.resource.PathResourceResolver;
//
//import javax.servlet.Servlet;
//import java.io.IOException;
//import java.util.List;
//
//@Configuration
//@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter.class})
//public class WebConfig extends WebMvcAutoConfiguration {
//
//    private Logger logger = LoggerFactory.getLogger(WebConfig.class);
//
//    @Configuration
//    @Import({WebMvcAutoConfiguration.EnableWebMvcConfiguration.class})
//    @EnableConfigurationProperties({WebMvcProperties.class, ResourceProperties.class})
//    public static class SaeWebMvcAutoConfigurationAdapter extends WebMvcAutoConfigurationAdapter {
//
//        private Logger logger = LoggerFactory.getLogger(SaeWebMvcAutoConfigurationAdapter.class);
//
//        @Autowired
//        private HttpMessageConverters messageConverters;
//        @Autowired
//        private ResourceProperties resourceProperties = new ResourceProperties();
//
//        @Override
//        public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
//            converters.add(new GensonHttpMessageConverter());
//            logger.debug("Registered " + GensonHttpMessageConverter.class.getName());
//            converters.addAll(this.messageConverters.getConverters());
//        }
//
//        @Override
//        public void addResourceHandlers(ResourceHandlerRegistry registry) {
//            Integer cachePeriod = resourceProperties.getCachePeriod();
//
//            /**
//             * Angular artefacts must not be mapped to index.html
//             */
//            registry.addResourceHandler("*.js", "*.css", "*.map", "flags.*", "icons.*", "favicon.ico")
//                    .addResourceLocations("classpath:/static/")
//                    .setCachePeriod(cachePeriod);
//
//            registry.addResourceHandler("assets/**")
//                    .addResourceLocations("classpath:/static/assets/")
//                    .setCachePeriod(cachePeriod);
//
//            registry.addResourceHandler("/static/**")
//                    .addResourceLocations("classpath:/static/")
//                    .setCachePeriod(cachePeriod);
//
//            /**
//             * For Angular all requests have to go to index.html
//             */
//            registry.addResourceHandler("/**")
//                    .addResourceLocations("classpath:/static/index.html")
//                    .setCachePeriod(cachePeriod).resourceChain(true)
//                    .addResolver(new PathResourceResolver() {
//                        @Override
//                        protected Resource getResource(String resourcePath, Resource location) throws IOException {
//                            return location.exists() && location.isReadable() ? location : null;
//                        }
//                    });
//        }
//    }
//}
