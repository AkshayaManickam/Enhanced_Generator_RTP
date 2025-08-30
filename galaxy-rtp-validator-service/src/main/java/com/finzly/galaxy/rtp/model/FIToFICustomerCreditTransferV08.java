package com.finzly.galaxy.rtp.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Financial Institution to Financial Institution Customer Credit Transfer V08
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class FIToFICustomerCreditTransferV08 {
    
    @XmlElement(name = "GrpHdr",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private GroupHeader93 grpHdr;
    
    @XmlElement(name = "CdtTrfTxInf",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private CreditTransferTransaction39 cdtTrfTxInf;
}
