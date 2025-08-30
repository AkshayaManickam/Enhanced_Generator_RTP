package com.finzly.galaxy.rtp.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Instruction for Creditor Agent
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class InstructionForCreditorAgent1 {
    
    @XmlElement(name = "Cd",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private String cd;
    
    @XmlElement(name = "InstrInf",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private String instrInf;
}
