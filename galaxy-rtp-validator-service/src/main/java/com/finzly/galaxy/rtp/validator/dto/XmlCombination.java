package com.finzly.galaxy.rtp.validator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XmlCombination {
    private int combinationNumber;
    private String description;
    private List<String> includedOptionalTags;
    private String xmlContent;
}

