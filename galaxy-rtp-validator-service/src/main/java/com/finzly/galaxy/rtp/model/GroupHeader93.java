package com.finzly.galaxy.rtp.model;

import com.finzly.galaxy.rtp.util.LocalDateAdapter;
import com.finzly.galaxy.rtp.util.LocalDateTimeAdapter;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Group Header for PACS.008 message
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class GroupHeader93 {
    
    @XmlElement(name = "MsgId",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private String msgId;

    @XmlElement(name = "CreDtTm",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime creDtTm;

    @XmlElement(name = "NbOfTxs",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private String nbOfTxs;
    
//    @XmlElement(name = "TtlIntrBkSttlmAmt",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
//    private ActiveCurrencyAndAmount ttlIntrBkSttlmAmt;

    @XmlElement(name = "TtlIntrBkSttlmAmt",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08",  required = true)
    private ActiveCurrencyAndAmount ttlIntrBkSttlmAmt;
    
//    @XmlElement(name = "IntrBkSttlmDt",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
//    @XmlSchemaType(name = "date")
//    private LocalDate intrBkSttlmDt;

    @XmlElement(name = "IntrBkSttlmDt",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate intrBkSttlmDt;
    
    @XmlElement(name = "SttlmInf",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private SettlementInstruction7 sttlmInf;
}
