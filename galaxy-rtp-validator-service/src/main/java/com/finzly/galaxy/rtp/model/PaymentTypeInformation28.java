package com.finzly.galaxy.rtp.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Payment Type Information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentTypeInformation28 {
    
    @XmlElement(name = "SvcLvl",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private ServiceLevel8Choice svcLvl;
    
    @XmlElement(name = "LclInstrm",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private LocalInstrument2Choice lclInstrm;
    
    @XmlElement(name = "CtgyPurp",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private CategoryPurpose1Choice ctgyPurp;
}
