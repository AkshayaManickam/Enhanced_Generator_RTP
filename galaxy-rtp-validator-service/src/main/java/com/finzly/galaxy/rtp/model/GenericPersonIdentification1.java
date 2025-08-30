package com.finzly.galaxy.rtp.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Generic Person Identification
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class GenericPersonIdentification1 {
    
    @XmlElement(name = "Id",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private String id;
    
    @XmlElement(name = "SchmeNm",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private PersonIdentificationSchemeName1Choice schmeNm;
}
