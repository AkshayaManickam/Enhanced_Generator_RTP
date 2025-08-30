package com.finzly.galaxy.rtp.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Remittance Location
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class RemittanceLocation7 {
    
    @XmlElement(name = "RmtId",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private String rmtId;
    
    @XmlElement(name = "RmtLctnDtls",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private RemittanceLocationData1 rmtLctnDtls;
}
