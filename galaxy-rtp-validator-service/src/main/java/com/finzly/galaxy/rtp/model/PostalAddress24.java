package com.finzly.galaxy.rtp.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Postal Address
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class PostalAddress24 {
    
    @XmlElement(name = "StrtNm",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private String strtNm;
    
    @XmlElement(name = "BldgNb",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private String bldgNb;
    
    @XmlElement(name = "PstCd",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private String pstCd;
    
    @XmlElement(name = "TwnNm",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private String twnNm;
    
    @XmlElement(name = "CtrySubDvsn",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private String ctrySubDvsn;
    
    @XmlElement(name = "Ctry",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private String ctry;
    
    @XmlElement(name = "AdrLine",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private String adrLine;
}
