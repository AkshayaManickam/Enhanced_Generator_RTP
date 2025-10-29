package com.finzly.galaxy.rtp.validator.controller;

import com.finzly.galaxy.rtp.validator.dto.TagSelectionRequest;
import com.finzly.galaxy.rtp.validator.dto.TagStatistics;
import com.finzly.galaxy.rtp.validator.dto.XmlGenerationResponse;
import com.finzly.galaxy.rtp.validator.dto.XmlGenerationResult;
import com.finzly.galaxy.rtp.validator.model.XmlTag;
import com.finzly.galaxy.rtp.validator.service.TagService;
import com.finzly.galaxy.rtp.validator.service.XmlGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@CrossOrigin(origins = "*")
public class TagController {

    @Autowired
    private TagService tagService;

    @Autowired
    private XmlGenerationService xmlGenerationService;

    @GetMapping
    public ResponseEntity<List<XmlTag>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @GetMapping("/search")
    public ResponseEntity<List<XmlTag>> searchTags(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(tagService.searchTags(query));
    }

    @GetMapping("/statistics")
    public ResponseEntity<TagStatistics> getStatistics() {
        return ResponseEntity.ok(tagService.getTagStatistics());
    }

    @PostMapping("/selection")
    public ResponseEntity<Void> updateTagSelection(@RequestBody TagSelectionRequest request) {
        tagService.updateTagSelection(request.getSelectedTagIndices());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/generate-xml-combinations")
    public ResponseEntity<XmlGenerationResult> generateXmlCombinations(@RequestBody TagSelectionRequest request) {
        XmlGenerationResult result = xmlGenerationService.generateXmlCombinations(request.getSelectedTagIndices());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/generate-xml")
    public ResponseEntity<XmlGenerationResponse> generateXml(@RequestBody TagSelectionRequest request) {
        // Check for conditional tags
        boolean hasConditional = request.getSelectedTagIndices().stream()
                .anyMatch(index -> {
                    XmlTag tag = tagService.getAllTags().stream()
                            .flatMap(root -> getAllTagsFlat(root).stream())
                            .filter(t -> t.getIndex().equals(index))
                            .findFirst()
                            .orElse(null);
                    return tag != null && tag.isConditional();
                });

        if (hasConditional) {
            return ResponseEntity.ok(XmlGenerationResponse.builder()
                    .success(false)
                    .message("Conditional tags are not allowed for XML generation Limited in this version")
                    .build());
        }

        // Update selection
        tagService.updateTagSelection(request.getSelectedTagIndices());

        return ResponseEntity.ok(XmlGenerationResponse.builder()
                .success(true)
                .message("XML generation successful (placeholder)")
                .xmlContent("<?xml version=\"1.0\" encoding=\"UTF-8\"?><!-- XML generation coming soon -->")
                .build());
    }

    private List<XmlTag> getAllTagsFlat(XmlTag tag) {
        List<XmlTag> result = new java.util.ArrayList<>();
        result.add(tag);
        for (XmlTag child : tag.getChildren()) {
            result.addAll(getAllTagsFlat(child));
        }
        return result;
    }
}

