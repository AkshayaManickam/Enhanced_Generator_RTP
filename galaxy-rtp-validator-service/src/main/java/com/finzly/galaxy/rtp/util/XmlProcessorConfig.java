package com.finzly.galaxy.rtp.util;

import com.finzly.galaxy.rtp.util.XmlProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;

@Configuration
public class XmlProcessorConfig {

    @Bean
    public XmlProcessor xmlProcessor() throws Exception {
        URL xsdUrl = getClass().getClassLoader().getResource("xsd/pacs.008.001.08.xsd");
        if (xsdUrl == null) throw new RuntimeException("XSD file not found");
        return new XmlProcessor(xsdUrl.toURI().getPath());
    }
}