package com.finzly.galaxy.rtp.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Remittance Information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class RemittanceInformation16 {
    
    @XmlElement(name = "Ustrd",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private String ustrd;
    
    @XmlElement(name = "Strd",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private StructuredRemittanceInformation16 strd;
}
