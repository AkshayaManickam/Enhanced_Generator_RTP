package com.finzly.galaxy.rtp.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Remittance Location Data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class RemittanceLocationData1 {
    
    @XmlElement(name = "Mtd",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private String mtd;
    
    @XmlElement(name = "ElctrncAdr",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private String elctrncAdr;
}
