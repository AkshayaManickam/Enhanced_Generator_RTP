package com.finzly.galaxy.rtp.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Financial Institution Identification
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class FinancialInstitutionIdentification18 {
    
    @XmlElement(name = "BICFI",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private String bicfi;
    
    @XmlElement(name = "ClrSysMmbId",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private ClearingSystemMemberIdentification2 clrSysMmbId;
}
