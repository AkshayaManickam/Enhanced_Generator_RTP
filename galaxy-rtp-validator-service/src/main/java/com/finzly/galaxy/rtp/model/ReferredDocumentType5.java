package com.finzly.galaxy.rtp.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Referred Document Type
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class ReferredDocumentType5 {
    
    @XmlElement(name = "CdOrPrtry",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private ReferredDocumentType5Choice cdOrPrtry;
}
