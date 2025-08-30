package com.finzly.galaxy.rtp.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Settlement Instruction for PACS.008 message
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class SettlementInstruction7 {
    
    @XmlElement(name = "SttlmMtd",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private String sttlmMtd;
    
    @XmlElement(name = "ClrSys",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private ClearingSystemIdentification3Choice clrSys;
}
