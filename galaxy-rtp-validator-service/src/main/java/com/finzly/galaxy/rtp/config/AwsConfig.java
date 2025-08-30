package com.finzly.galaxy.rtp.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "spring.cloud.aws.enabled", havingValue = "true", matchIfMissing = false)
public class AwsConfig {
    // This configuration will only be loaded if AWS is explicitly enabled
}
