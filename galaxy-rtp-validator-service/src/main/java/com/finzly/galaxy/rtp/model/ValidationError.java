package com.finzly.galaxy.rtp.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Validation error model for API responses
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationError {
    
    private String code;
    private String message;
    private String location;
    private String severity;
    private String category;
}
