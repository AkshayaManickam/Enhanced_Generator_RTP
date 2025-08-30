package com.finzly.galaxy.rtp.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Main document class representing PACS.008 Financial Institution to Financial Institution
 * Customer Credit Transfer message.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "Document", namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
@XmlAccessorType(XmlAccessType.FIELD)
public class Document {
    
    @XmlElement(name = "FIToFICstmrCdtTrf",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08",  required = true)
    private FIToFICustomerCreditTransferV08 fiToFICstmrCdtTrf;
}
