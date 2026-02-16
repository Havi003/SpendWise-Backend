package com.smartcoach.spendwise.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "merchants")

public class MerchantRulesConfig {
// Initialize to an empty map to prevent NullPointerExceptions 
    // if the YAML fails to load or is empty
    private Map<String, String> library = new HashMap<>();

    public Map<String, String> getLibrary() {
        return library;
    }

    public void setLibrary(Map<String, String> library) {
        this.library = library;
    }

}
