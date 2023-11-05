package com.license;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LicenseAutoConfiguration {
    @Bean
    public LicenseTemplate licenseTemplate() {
        return new LicenseTemplate();
    }

    @Bean
    public LicenseTemplateService licenseTemplateService() {
        return new LicenseTemplateService();
    }
}
