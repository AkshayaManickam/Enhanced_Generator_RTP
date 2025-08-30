package com.finzly.galaxy.rtp.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Generic Account Identification
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class GenericAccountIdentification1 {
    
    @XmlElement(name = "Id",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private String id;
}
