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
public class XmlGenerationResult {
    private boolean success;
    private String message;
    private int totalCombinations;
    private List<XmlCombination> combinations;
    private long generationTimeMs;
}

