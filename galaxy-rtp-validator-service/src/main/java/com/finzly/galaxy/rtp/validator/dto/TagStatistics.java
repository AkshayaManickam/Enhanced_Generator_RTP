package com.finzly.galaxy.rtp.validator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagStatistics {
    private long totalTags;
    private long mandatoryTags;
    private long optionalTags;
    private long conditionalTags;
    private long selectedTags;
}

