package com.finzly.galaxy.rtp.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Service
public class TenantEnvConfigService {

    public Properties loadTenantEnvProperties(String tenant, String env) throws IOException {
        String filename = "config/" + tenant + "-" + env + ".properties";
        ClassPathResource resource = new ClassPathResource(filename);

        Properties props = new Properties();
        try (InputStream input = resource.getInputStream()) {
            props.load(input);
        }

        return props;
    }

    public String getProperty(String tenant, String env, String key) throws IOException {
        return loadTenantEnvProperties(tenant, env).getProperty(key);
    }
}