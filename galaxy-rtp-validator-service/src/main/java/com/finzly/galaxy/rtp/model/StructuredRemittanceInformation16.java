package com.finzly.galaxy.rtp.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * Structured Remittance Information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class StructuredRemittanceInformation16 {
    
    @XmlElement(name = "RfrdDocInf",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private List<ReferredDocumentInformation7> rfrdDocInf;
    
    @XmlElement(name = "RfrdDocAmt",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private RemittanceAmount2 rfrdDocAmt;
    
    @XmlElement(name = "Invcr",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private PartyIdentification135 invcr;
    
    @XmlElement(name = "Invcee",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private PartyIdentification135 invcee;
}
