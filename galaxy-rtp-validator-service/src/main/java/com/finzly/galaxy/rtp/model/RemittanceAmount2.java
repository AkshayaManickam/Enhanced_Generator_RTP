package com.finzly.galaxy.rtp.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * Remittance Amount
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class RemittanceAmount2 {
    
    @XmlElement(name = "DscntApldAmt",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private List<DiscountAmountAndType1> dscntApldAmt;
}
