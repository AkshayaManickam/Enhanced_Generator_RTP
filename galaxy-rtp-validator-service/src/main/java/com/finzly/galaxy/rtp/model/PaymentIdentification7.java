package com.finzly.galaxy.rtp.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Payment Identification Information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentIdentification7 {
    
    @XmlElement(name = "InstrId",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private String instrId;
    
    @XmlElement(name = "EndToEndId",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private String endToEndId;
    
    @XmlElement(name = "TxId",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private String txId;
    
    @XmlElement(name = "UETR",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private String uetr;
    
    @XmlElement(name = "ClrSysRef",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private String clrSysRef;
}
