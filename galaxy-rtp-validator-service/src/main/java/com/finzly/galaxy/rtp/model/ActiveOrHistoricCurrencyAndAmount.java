package com.finzly.galaxy.rtp.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

/**
 * Active or Historic Currency and Amount
 */
@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class ActiveOrHistoricCurrencyAndAmount {
    
    @XmlValue
    private BigDecimal value;
    
    @XmlAttribute(name = "Ccy",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private String ccy;
    
    public ActiveOrHistoricCurrencyAndAmount(BigDecimal value, String ccy) {
        this.value = value;
        this.ccy = ccy;
    }
}
