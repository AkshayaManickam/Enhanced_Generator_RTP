package com.finzly.galaxy.rtp.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

/**
 * Referred Document Information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class ReferredDocumentInformation7 {
    
    @XmlElement(name = "Tp",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private ReferredDocumentType5 tp;
    
    @XmlElement(name = "Nb",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private String nb;
    
    @XmlElement(name = "RltdDt",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    @XmlSchemaType(name = "date")
    private LocalDate rltdDt;
}
