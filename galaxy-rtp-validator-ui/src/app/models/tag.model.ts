export enum TagType {
  MANDATORY = 'M',
  OPTIONAL = 'O',
  CONDITIONAL = 'C'
}

export interface XmlTag {
  index: string;
  xmlTag: string;
  elementName: string;
  occurrence: string;
  length?: number;
  type: TagType;
  selected: boolean;
  orCondition?: boolean;
  level: number;
  children: XmlTag[];
}

export interface TagStatistics {
  totalTags: number;
  mandatoryTags: number;
  optionalTags: number;
  conditionalTags: number;
  selectedTags: number;
}

export interface TagSelectionRequest {
  selectedTagIndices: string[];
}

export interface XmlGenerationResponse {
  success: boolean;
  message: string;
  xmlContent?: string;
}

export interface XmlCombination {
  combinationNumber: number;
  description: string;
  includedOptionalTags: string[];
  xmlContent: string;
}

export interface XmlGenerationResult {
  success: boolean;
  message: string;
  totalCombinations: number;
  combinations: XmlCombination[];
  generationTimeMs: number;
}

