package com.finzly.galaxy.rtp.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

/**
 * Date and Place of Birth
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class DateAndPlaceOfBirth1 {
    
    @XmlElement(name = "BirthDt",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    @XmlSchemaType(name = "date")
    private LocalDate birthDt;
    
    @XmlElement(name = "CityOfBirth",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private String cityOfBirth;
    
    @XmlElement(name = "CtryOfBirth",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08", required = true)
    private String ctryOfBirth;
}
