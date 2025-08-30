package com.finzly.galaxy.rtp.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Cash Account Information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class CashAccount38 {
    
    @XmlElement(name = "Id",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private AccountIdentification4Choice id;
    
    @XmlElement(name = "Nm",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private String nm;
    
    @XmlElement(name = "Prxy",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private ProxyAccountIdentification1 prxy;
}
