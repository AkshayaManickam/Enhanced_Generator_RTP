package com.finzly.galaxy.rtp.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Discount Amount Type Choice
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class DiscountAmountType1Choice {
    
    @XmlElement(name = "Prtry",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private String prtry;
}
