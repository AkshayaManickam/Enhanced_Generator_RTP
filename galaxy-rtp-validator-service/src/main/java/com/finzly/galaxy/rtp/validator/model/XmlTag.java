package com.finzly.galaxy.rtp.validator.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class XmlTag {
    
    private String index;
    private String xmlTag;
    private String elementName;
    private String occurrence;
    private Integer length;
    private TagType type;
    private boolean selected;
    private boolean orCondition;
    private Integer level;
    
    @Builder.Default
    private List<XmlTag> children = new ArrayList<>();
    
    public boolean isMandatory() {
        return type == TagType.MANDATORY;
    }
    
    public boolean isOptional() {
        return type == TagType.OPTIONAL;
    }
    
    public boolean isConditional() {
        return type == TagType.CONDITIONAL;
    }
}

