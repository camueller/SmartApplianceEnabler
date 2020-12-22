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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.io.IOException;
import java.util.List;

@EnableWebMvc
@ComponentScan(basePackages = "de.avanux.smartapplianceenabler")
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private Logger logger = LoggerFactory.getLogger(WebConfig.class);

    @Autowired
    private HttpMessageConverters messageConverters;

    @Bean
    public InternalResourceViewResolver defaultViewResolver() {
        return new InternalResourceViewResolver();
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/static/index.html");
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new GensonHttpMessageConverter());
        logger.debug("Registered " + GensonHttpMessageConverter.class.getName());
        converters.addAll(this.messageConverters.getConverters());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        CacheControl noCache = CacheControl.noCache().cachePublic();

        /**
         * Angular artefacts must not be mapped to index.html
         */
        registry.addResourceHandler("*.js", "*.css", "*.map", "flags.*", "icons.*", "favicon.ico")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(noCache);

        registry.addResourceHandler("assets/**")
                .addResourceLocations("classpath:/static/assets/")
                .setCacheControl(noCache);

        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(noCache);

        /**
         * For Angular all requests have to go to index.html
         */
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/index.html")
                .setCacheControl(noCache)
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        return location.exists() && location.isReadable() ? location : null;
                    }
                });
    }
}
