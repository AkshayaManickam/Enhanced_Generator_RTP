package com.finzly.galaxy.rtp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@ComponentScan(value = {
                "com.finzly.galaxy.rtp"
})
@EnableJpaRepositories(basePackages = { "com.finzly.galaxy.rtp.validator.repository" })
@EntityScan(basePackages = {
                "com.finzly.galaxy.rtp.**",
                "com.finzly.galaxy.rtp"
})
@EnableTransactionManagement
public class RTPValidatorApplication {

        public static void main(String[] args) {
                SpringApplication.run(RTPValidatorApplication.class, args);
        }

}
