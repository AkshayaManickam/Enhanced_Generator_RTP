package com.finzly.galaxy.rtp.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Discount Amount and Type
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class DiscountAmountAndType1 {
    
    @XmlElement(name = "Tp",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private DiscountAmountType1Choice tp;
    
    @XmlElement(name = "Amt",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private ActiveOrHistoricCurrencyAndAmount amt;
}
