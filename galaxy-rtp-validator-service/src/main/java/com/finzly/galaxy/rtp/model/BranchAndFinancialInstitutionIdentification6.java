package com.finzly.galaxy.rtp.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Branch and Financial Institution Identification
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class BranchAndFinancialInstitutionIdentification6 {
    
    @XmlElement(name = "FinInstnId",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private FinancialInstitutionIdentification18 finInstnId;
}
