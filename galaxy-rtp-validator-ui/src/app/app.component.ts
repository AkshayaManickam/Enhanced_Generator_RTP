import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { TagService } from './services/tag.service';
import { XmlTag, TagStatistics, TagType, XmlGenerationResult, XmlCombination } from './models/tag.model';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  title = 'PACS.008 Message Generator';
  
  // Wizard steps
  currentStep: number = 1;
  totalSteps: number = 3;
  
  // Tag data
  tags: XmlTag[] = [];
  statistics: TagStatistics | null = null;
  searchQuery: string = '';
  loading: boolean = false;
  message: string = '';
  messageType: 'success' | 'error' | 'info' = 'info';
  expandedNodes: Set<string> = new Set();

  // Generation results
  generationResult: XmlGenerationResult | null = null;
  generating: boolean = false;
  selectedCombination: XmlCombination | null = null;

  TagType = TagType;
  Math = Math; // Expose Math to template

  constructor(private tagService: TagService, private sanitizer: DomSanitizer) {}

  // Check if checkbox should be disabled
  isCheckboxDisabled(tag: XmlTag): boolean {
    return tag.type === TagType.MANDATORY || tag.type === TagType.CONDITIONAL;
  }

  // Get tooltip text based on tag type and selection state
  getCheckboxTooltip(tag: XmlTag): string {
    if (tag.type === TagType.MANDATORY) {
      return 'Mandatory field - Auto-selected by system (Cannot be deselected)';
    }
    if (tag.type === TagType.CONDITIONAL) {
      return 'Conditional field - Blocked by system (Cannot be selected)';
    }
    return tag.selected ? 'Click to deselect this optional tag' : 'Click to select this optional tag';
  }

  ngOnInit(): void {
    this.loadTags();
    this.loadStatistics();
  }

  // Navigation methods
  nextStep(): void {
    if (this.currentStep < this.totalSteps) {
      if (this.currentStep === 1) {
        // Validate that at least one optional tag is selected
        const selectedOptional = this.getSelectedOptionalTags();
        if (selectedOptional.length === 0) {
          this.showMessage('Please select at least one optional tag to proceed.', 'info');
          return;
        }
        
        // Validate no conditional tags are selected
        const hasConditional = this.checkForConditionalTags(this.tags);
        if (hasConditional) {
          this.showMessage('Please deselect all conditional tags before proceeding. Conditional tags are not supported.', 'error');
          return;
        }
      }
      
      if (this.currentStep === 2) {
        // Auto-generate when moving from step 2 to step 3
        if (!this.generationResult) {
          this.generateCombinations();
        }
      }
      
      this.currentStep++;
    }
  }

  previousStep(): void {
    if (this.currentStep > 1) {
      this.currentStep--;
    }
  }

  goToStep(step: number): void {
    if (step <= this.currentStep || step === 1) {
      this.currentStep = step;
    }
  }

  // Load data methods
  loadTags(): void {
    this.loading = true;
    this.tagService.getAllTags().subscribe({
      next: (data) => {
        this.tags = data;
        this.loading = false;
        
        // Auto-select all top-level mandatory tags and their mandatory children
        this.autoSelectTopLevelMandatoryTags(this.tags);
        
        // Auto-expand first level
        this.tags.forEach(tag => {
          this.expandedNodes.add(tag.index);
          this.expandFirstLevelChildren(tag);
        });
        
        // Refresh statistics after auto-selection
        this.loadStatistics();
      },
      error: (error) => {
        console.error('Error loading tags:', error);
        this.showMessage('Error loading tags. Please make sure the backend is running.', 'error');
        this.loading = false;
      }
    });
  }

  // Auto-select all top-level mandatory tags (not nested in optional/conditional parents)
  autoSelectTopLevelMandatoryTags(tags: XmlTag[]): void {
    tags.forEach(tag => {
      if (tag.type === TagType.MANDATORY) {
        tag.selected = true;
        // Recursively select all mandatory children
        this.selectAllMandatoryDescendants(tag);
      }
      // Don't auto-select children of optional/conditional tags
    });
  }

  // Recursively select all mandatory descendants
  selectAllMandatoryDescendants(tag: XmlTag): void {
    if (tag.children) {
      tag.children.forEach(child => {
        if (child.type === TagType.MANDATORY) {
          child.selected = true;
          this.selectAllMandatoryDescendants(child);
        }
      });
    }
  }

  expandFirstLevelChildren(tag: XmlTag): void {
    if (tag.children && tag.children.length > 0) {
      tag.children.forEach(child => {
        this.expandedNodes.add(child.index);
      });
    }
  }

  loadStatistics(): void {
    this.tagService.getStatistics().subscribe({
      next: (data) => {
        this.statistics = data;
      },
      error: (error) => {
        console.error('Error loading statistics:', error);
      }
    });
  }

  onSearch(): void {
    if (this.searchQuery.trim()) {
      this.loading = true;
      this.tagService.searchTags(this.searchQuery).subscribe({
        next: (data) => {
          this.tags = data;
          this.loading = false;
          // Expand all search results
          this.expandAllNodes(this.tags);
        },
        error: (error) => {
          console.error('Error searching tags:', error);
          this.loading = false;
        }
      });
    } else {
      this.loadTags();
    }
  }

  expandAllNodes(tags: XmlTag[]): void {
    tags.forEach(tag => {
      this.expandedNodes.add(tag.index);
      if (tag.children && tag.children.length > 0) {
        this.expandAllNodes(tag.children);
      }
    });
  }

  toggleExpand(index: string): void {
    if (this.expandedNodes.has(index)) {
      this.expandedNodes.delete(index);
    } else {
      this.expandedNodes.add(index);
    }
  }

  isExpanded(index: string): boolean {
    return this.expandedNodes.has(index);
  }

  // Tag selection logic with proper parent-child relationships
  onTagToggle(tag: XmlTag, event: Event): void {
    event.stopPropagation();
    
    console.log('Tag clicked:', {
      index: tag.index,
      name: tag.xmlTag,
      type: tag.type,
      selected: tag.selected,
      disabled: this.isCheckboxDisabled(tag)
    });
    
    // MANDATORY tags - System controlled, user cannot deselect
    // Checkboxes are disabled so this should never be called, but adding safeguard
    if (tag.type === TagType.MANDATORY) {
      console.warn('Attempted to toggle MANDATORY tag - blocked by system');
      event.preventDefault();
      return; // Silently block - controlled by system
    }

    // CONDITIONAL tags - Completely blocked by system
    // Checkboxes are disabled so this should never be called, but adding safeguard
    if (tag.type === TagType.CONDITIONAL) {
      console.warn('Attempted to toggle CONDITIONAL tag - blocked by system');
      event.preventDefault();
      return; // Silently block - not supported
    }

    // Only OPTIONAL tags can be toggled by user
    if (tag.type === TagType.OPTIONAL) {
      console.log('Toggling OPTIONAL tag');
      tag.selected = !tag.selected;
      
      if (tag.selected) {
        // When selecting an optional tag:
        // 1. Auto-select all its mandatory children (system-controlled)
        this.selectAllMandatoryChildren(tag);
        // 2. Auto-expand to show the children
        this.expandedNodes.add(tag.index);
        this.expandAllChildren(tag);
      } else {
        // When deselecting an optional tag:
        // Deselect ALL children (mandatory, optional, conditional)
        this.deselectAllChildren(tag);
      }
      
      // Refresh statistics
      this.loadStatistics();
    }
  }

  // Check if a tag has an optional or conditional ancestor
  hasOptionalOrConditionalAncestor(tag: XmlTag): boolean {
    // This would require parent references, which we don't have in the current structure
    // For now, we'll use a simpler approach: check if tag is at root level
    return this.isNestedTag(tag);
  }

  // Check if tag is nested (not at root level)
  isNestedTag(tag: XmlTag): boolean {
    // A tag is nested if its level > 0 or if we can find it in children
    return tag.level > 0;
  }

  // Select all mandatory children recursively
  selectAllMandatoryChildren(tag: XmlTag): void {
    if (tag.children) {
      console.log(`Checking children of ${tag.xmlTag}:`, tag.children.map(c => ({
        name: c.xmlTag,
        type: c.type,
        willSelect: c.type === TagType.MANDATORY
      })));
      
      tag.children.forEach(child => {
        if (child.type === TagType.MANDATORY) {
          console.log(`  ✅ Auto-selecting MANDATORY child: ${child.xmlTag} (${child.index})`);
          child.selected = true;
          // Recursively select mandatory descendants
          this.selectAllMandatoryChildren(child);
        } else if (child.type === TagType.OPTIONAL) {
          console.log(`  ⏭️  Skipping OPTIONAL child: ${child.xmlTag} (${child.index})`);
          // Don't auto-select optional children
        } else if (child.type === TagType.CONDITIONAL) {
          console.log(`  🚫 Skipping CONDITIONAL child: ${child.xmlTag} (${child.index})`);
          // Conditional children remain blocked
        }
      });
    }
  }

  // Expand all children recursively
  expandAllChildren(tag: XmlTag): void {
    if (tag.children && tag.children.length > 0) {
      tag.children.forEach(child => {
        this.expandedNodes.add(child.index);
        this.expandAllChildren(child);
      });
    }
  }

  deselectAllChildren(tag: XmlTag): void {
    if (tag.children) {
      console.log(`Deselecting all children of ${tag.xmlTag}`);
      tag.children.forEach(child => {
        console.log(`  ❌ Deselecting child: ${child.xmlTag} (${child.index}) - Type: ${child.type}`);
        // Deselect ALL children when parent optional tag is deselected
        child.selected = false;
        // Recursively deselect all descendants
        this.deselectAllChildren(child);
      });
    }
  }

  updateChildrenSelection(tag: XmlTag, selected: boolean): void {
    if (tag.children) {
      tag.children.forEach(child => {
        if (child.type !== TagType.MANDATORY && child.type !== TagType.CONDITIONAL) {
          child.selected = selected;
          this.updateChildrenSelection(child, selected);
        }
      });
    }
  }

  getSelectedIndices(tags: XmlTag[]): string[] {
    let indices: string[] = [];
    tags.forEach(tag => {
      if (tag.selected) {
        indices.push(tag.index);
      }
      if (tag.children && tag.children.length > 0) {
        indices = indices.concat(this.getSelectedIndices(tag.children));
      }
    });
    return indices;
  }

  getSelectedOptionalTags(): XmlTag[] {
    return this.getAllOptionalTags(this.tags).filter(tag => tag.selected);
  }

  getAllOptionalTags(tags: XmlTag[]): XmlTag[] {
    let optionalTags: XmlTag[] = [];
    tags.forEach(tag => {
      if (tag.type === TagType.OPTIONAL) {
        optionalTags.push(tag);
      }
      if (tag.children && tag.children.length > 0) {
        optionalTags = optionalTags.concat(this.getAllOptionalTags(tag.children));
      }
    });
    return optionalTags;
  }

  // XML Generation
  generateCombinations(): void {
    const selectedIndices = this.getSelectedIndices(this.tags);
    
    // Check for conditional tags
    const hasConditional = this.checkForConditionalTags(this.tags);
    if (hasConditional) {
      this.showMessage('Conditional tags are not supported in this version', 'error');
      return;
    }

    this.generating = true;
    this.generationResult = null;

    this.tagService.generateXmlCombinations({ selectedTagIndices: selectedIndices }).subscribe({
      next: (result) => {
        this.generationResult = result;
        this.generating = false;
        if (result.success) {
          this.showMessage(`Successfully generated ${result.totalCombinations} XML combinations in ${result.generationTimeMs}ms`, 'success');
        } else {
          this.showMessage(result.message, 'error');
        }
      },
      error: (error) => {
        console.error('Error generating XML combinations:', error);
        this.showMessage('Error generating XML combinations.', 'error');
        this.generating = false;
      }
    });
  }

  checkForConditionalTags(tags: XmlTag[]): boolean {
    for (const tag of tags) {
      if (tag.selected && tag.type === TagType.CONDITIONAL) {
        return true;
      }
      if (tag.children && tag.children.length > 0) {
        if (this.checkForConditionalTags(tag.children)) {
          return true;
        }
      }
    }
    return false;
  }

  viewCombination(combination: XmlCombination): void {
    this.selectedCombination = combination;
  }

  // Process XML to highlight tags
  getHighlightedXml(xmlContent: string): SafeHtml {
    if (!xmlContent) return '';
    
    console.log('Original XML contains markers:', xmlContent.includes('<!--OPTIONAL_START-->'));
    console.log('First 500 chars of XML:', xmlContent.substring(0, 500));
    
    // First, escape HTML characters EXCEPT our markers
    let processed = xmlContent
      // Temporarily replace our markers with placeholders
      .replace(/<!--OPTIONAL_START-->/g, '___OPTIONAL_START___')
      .replace(/<!--OPTIONAL_END-->/g, '___OPTIONAL_END___')
      .replace(/<!--OPTIONAL_TAG-->/g, '___OPTIONAL_TAG___')
      // Escape all HTML special characters
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;')
      // Now replace placeholders with HTML spans
      .replace(/___OPTIONAL_START___/g, '<span class="optional-tag">')
      .replace(/___OPTIONAL_END___/g, '</span>')
      .replace(/___OPTIONAL_TAG___/g, '');
    
    // Highlight all XML tags in dark color
    // Match opening tags: <tagname...>
    processed = processed.replace(/&lt;([^\/\s][^&gt;]*)&gt;/g, '<span class="xml-tag-bracket">&lt;</span><span class="xml-tag">$1</span><span class="xml-tag-bracket">&gt;</span>');
    // Match closing tags: </tagname>
    processed = processed.replace(/&lt;(\/[^&gt;]+)&gt;/g, '<span class="xml-tag-bracket">&lt;</span><span class="xml-tag">$1</span><span class="xml-tag-bracket">&gt;</span>');
    
    console.log('Processed XML contains spans:', processed.includes('<span class="optional-tag">'));
    console.log('First 500 chars of processed:', processed.substring(0, 500));
    
    // Use DomSanitizer to bypass Angular's security
    return this.sanitizer.bypassSecurityTrustHtml(processed);
  }

  downloadCombination(combination: XmlCombination): void {
    const blob = new Blob([combination.xmlContent], { type: 'application/xml' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `pacs008_combination_${combination.combinationNumber}.xml`;
    link.click();
    window.URL.revokeObjectURL(url);
    this.showMessage(`Downloaded combination ${combination.combinationNumber}`, 'success');
  }

  copyCombination(combination: XmlCombination): void {
    // Copy the raw XML content (without HTML markers) to clipboard
    const rawXml = combination.xmlContent;
    navigator.clipboard.writeText(rawXml).then(() => {
      this.showMessage('XML copied to clipboard', 'success');
    }).catch(err => {
      console.error('Failed to copy:', err);
      this.showMessage('Failed to copy XML', 'error');
    });
  }

  downloadAllCombinations(): void {
    if (!this.generationResult || !this.generationResult.combinations) return;

    this.generationResult.combinations.forEach((combination, index) => {
      setTimeout(() => {
        this.downloadCombination(combination);
      }, index * 100); // Stagger downloads
    });
  }

  resetWizard(): void {
    this.currentStep = 1;
    this.generationResult = null;
    this.selectedCombination = null;
    this.searchQuery = '';
    this.message = '';
    this.loadTags(); // This will auto-select top-level mandatory tags
    this.showMessage('Wizard reset. Mandatory tags are pre-selected for ISO 20022 compliance.', 'info');
  }

  showMessage(message: string, type: 'success' | 'error' | 'info'): void {
    this.message = message;
    this.messageType = type;
    setTimeout(() => {
      this.message = '';
    }, 5000);
  }

  getTagClass(type: TagType): string {
    switch (type) {
      case TagType.MANDATORY:
        return 'mandatory';
      case TagType.OPTIONAL:
        return 'optional';
      case TagType.CONDITIONAL:
        return 'conditional';
      default:
        return '';
    }
  }

  getIndentStyle(level: number): any {
    return {
      'padding-left': `${level * 20}px`
    };
  }

  getStepClass(step: number): string {
    if (step === this.currentStep) {
      return 'active';
    } else if (step < this.currentStep) {
      return 'completed';
    }
    return 'pending';
  }
}
