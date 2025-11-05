package com.finzly.galaxy.rtp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class TenantEnvConfigService {
    
    private final ConcurrentHashMap<String, Properties> propertiesCache = new ConcurrentHashMap<>();

    public Properties loadTenantEnvProperties(String tenant, String env) throws IOException {
        String cacheKey = tenant.toLowerCase() + "-" + env.toLowerCase();
        
        // Check cache first
        if (propertiesCache.containsKey(cacheKey)) {
            log.info("Returning cached properties for {}", cacheKey);
            return propertiesCache.get(cacheKey);
        }
        
        String filename = "config/" + tenant.toLowerCase() + "-" + env.toLowerCase() + ".properties";
        log.info("Loading properties from: {}", filename);
        
        ClassPathResource resource = new ClassPathResource(filename);

        Properties props = new Properties();
        try (InputStream input = resource.getInputStream()) {
            props.load(input);
            log.info("Successfully loaded {} properties from {}", props.size(), filename);
            
            // Cache the loaded properties
            propertiesCache.put(cacheKey, props);
        } catch (IOException e) {
            log.error("Failed to load properties from {}: {}", filename, e.getMessage());
            throw e;
        }

        return props;
    }

    public String getProperty(String tenant, String env, String key) {
        try {
            Properties props = loadTenantEnvProperties(tenant, env);
            String value = props.getProperty(key);
            
            if (value != null) {
                log.info("Property [{}] = {}", key, key.contains("secret") ? "***" : value);
                return value;
            } else {
                log.warn("Property [{}] not found for tenant={}, env={}", key, tenant, env);
                return "";
            }
        } catch (IOException e) {
            log.error("Error loading properties for tenant={}, env={}: {}", tenant, env, e.getMessage());
            return "";
        }
    }
    
    public void clearCache() {
        propertiesCache.clear();
        log.info("Properties cache cleared");
    }
}
