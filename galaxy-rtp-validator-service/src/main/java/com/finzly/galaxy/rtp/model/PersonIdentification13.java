package com.finzly.galaxy.rtp.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * Person Identification
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class PersonIdentification13 {
    
    @XmlElement(name = "DtAndPlcOfBirth",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private DateAndPlaceOfBirth1 dtAndPlcOfBirth;
    
    @XmlElement(name = "Othr",namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08")
    private List<GenericPersonIdentification1> othr;
}
