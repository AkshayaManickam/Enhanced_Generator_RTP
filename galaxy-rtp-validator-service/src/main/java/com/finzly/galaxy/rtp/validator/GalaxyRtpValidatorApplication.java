package com.finzly.galaxy.rtp.validator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.finzly.galaxy.rtp.validator",
    "com.finzly.galaxy.rtp.payment",
    "com.finzly.galaxy.rtp.config",
    "com.finzly.galaxy.rtp.mapper"
})
public class GalaxyRtpValidatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(GalaxyRtpValidatorApplication.class, args);
    }
}
