package com.finzly.galaxy.rtp.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Account Identification Choice
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class AccountIdentification4Choice {
    
    @XmlElement(name = "IBAN",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private String iban;
    
    @XmlElement(name = "Othr",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private GenericAccountIdentification1 othr;
}
