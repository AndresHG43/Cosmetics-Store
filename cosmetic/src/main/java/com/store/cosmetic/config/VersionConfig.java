package com.store.cosmetic.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class VersionConfig implements WebMvcConfigurer {
    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        configurer.addSupportedVersions("1");
        configurer.detectSupportedVersions(true);
        configurer.setDefaultVersion("1");
        configurer.useRequestHeader("X-API-Version");
    }
}
