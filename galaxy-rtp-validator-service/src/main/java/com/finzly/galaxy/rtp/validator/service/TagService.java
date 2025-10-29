package com.finzly.galaxy.rtp.validator.service;

import com.finzly.galaxy.rtp.validator.dto.TagStatistics;
import com.finzly.galaxy.rtp.validator.model.TagType;
import com.finzly.galaxy.rtp.validator.model.XmlTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TagService {

    @Autowired
    private TagDataService tagDataService;

    public List<XmlTag> getAllTags() {
        return tagDataService.getAllTags();
    }

    public List<XmlTag> searchTags(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllTags();
        }
        
        String lowerSearch = searchTerm.toLowerCase();
        List<XmlTag> results = new ArrayList<>();
        
        for (XmlTag rootTag : tagDataService.getAllTags()) {
            XmlTag filteredTag = filterTagsRecursively(rootTag, lowerSearch);
            if (filteredTag != null) {
                results.add(filteredTag);
            }
        }
        
        return results;
    }

    private XmlTag filterTagsRecursively(XmlTag tag, String searchTerm) {
        boolean matches = tag.getXmlTag().toLowerCase().contains(searchTerm) ||
                         tag.getElementName().toLowerCase().contains(searchTerm) ||
                         tag.getIndex().contains(searchTerm);

        List<XmlTag> filteredChildren = new ArrayList<>();
        for (XmlTag child : tag.getChildren()) {
            XmlTag filteredChild = filterTagsRecursively(child, searchTerm);
            if (filteredChild != null) {
                filteredChildren.add(filteredChild);
            }
        }

        if (matches || !filteredChildren.isEmpty()) {
            XmlTag filtered = XmlTag.builder()
                    .index(tag.getIndex())
                    .xmlTag(tag.getXmlTag())
                    .elementName(tag.getElementName())
                    .occurrence(tag.getOccurrence())
                    .length(tag.getLength())
                    .type(tag.getType())
                    .selected(tag.isSelected())
                    .orCondition(tag.isOrCondition())
                    .level(tag.getLevel())
                    .children(filteredChildren)
                    .build();
            return filtered;
        }

        return null;
    }

    public TagStatistics getTagStatistics() {
        long total = 0;
        long mandatory = 0;
        long optional = 0;
        long conditional = 0;
        long selected = 0;

        for (XmlTag rootTag : tagDataService.getAllTags()) {
            TagCounts counts = countTagsRecursively(rootTag);
            total += counts.total;
            mandatory += counts.mandatory;
            optional += counts.optional;
            conditional += counts.conditional;
            selected += counts.selected;
        }

        return TagStatistics.builder()
                .totalTags(total)
                .mandatoryTags(mandatory)
                .optionalTags(optional)
                .conditionalTags(conditional)
                .selectedTags(selected)
                .build();
    }

    private TagCounts countTagsRecursively(XmlTag tag) {
        TagCounts counts = new TagCounts();
        counts.total = 1;
        
        if (tag.getType() == TagType.MANDATORY) {
            counts.mandatory = 1;
        } else if (tag.getType() == TagType.OPTIONAL) {
            counts.optional = 1;
        } else if (tag.getType() == TagType.CONDITIONAL) {
            counts.conditional = 1;
        }
        
        if (tag.isSelected()) {
            counts.selected = 1;
        }

        for (XmlTag child : tag.getChildren()) {
            TagCounts childCounts = countTagsRecursively(child);
            counts.total += childCounts.total;
            counts.mandatory += childCounts.mandatory;
            counts.optional += childCounts.optional;
            counts.conditional += childCounts.conditional;
            counts.selected += childCounts.selected;
        }

        return counts;
    }

    public void updateTagSelection(List<String> selectedIndices) {
        // Reset all non-mandatory tags
        resetNonMandatoryTags();
        
        // Apply selections
        if (selectedIndices != null) {
            for (String index : selectedIndices) {
                XmlTag tag = tagDataService.getTagByIndex(index);
                if (tag != null && !tag.isConditional()) {
                    tag.setSelected(true);
                }
            }
        }
    }

    private void resetNonMandatoryTags() {
        for (XmlTag rootTag : tagDataService.getAllTags()) {
            resetNonMandatoryTagsRecursively(rootTag);
        }
    }

    private void resetNonMandatoryTagsRecursively(XmlTag tag) {
        if (!tag.isMandatory()) {
            tag.setSelected(false);
        }
        for (XmlTag child : tag.getChildren()) {
            resetNonMandatoryTagsRecursively(child);
        }
    }

    private static class TagCounts {
        long total = 0;
        long mandatory = 0;
        long optional = 0;
        long conditional = 0;
        long selected = 0;
    }
}

