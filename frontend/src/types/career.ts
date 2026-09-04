export type ResumeParseStatus = "CREATED" | "PARSING" | "PARSED" | "FAILED";

export interface ResumeVersion {
  id: number;
  resumeId: string;
  version: number;
  contentHash: string;
  parseStatus: ResumeParseStatus;
  createdAt: string;
  updatedAt: string;
}

export interface StructuredResume {
  education: Array<{ school: string; degree: string; field: string; period: string }>;
  projects: Array<{ id: string; name: string; summary: string; skills: string[]; highlights: string[] }>;
  workExperience: Array<{ id: string; organization: string; role: string; summary: string; skills: string[] }>;
  skills: Array<{ name: string; level: string }>;
  achievements: Array<{ id: string; title: string; detail: string; skills: string[] }>;
}

export interface CandidateProfile {
  id: number;
  resumeVersionId: number;
  profileVersion: number;
  schemaVersion: string;
  profile: StructuredResume;
  generatedAt: string;
}

export interface CandidateEvidence {
  id: number;
  resumeVersionId: number;
  profileSnapshotId: number;
  sourceType: string;
  sourceId: string;
  sourceReference: string;
  skillCode: string;
  claim: string;
  evidenceText: string;
  confidence: number;
  createdAt: string;
}

export type JobRequirementParseStatus = "CREATED" | "PARSING" | "PARSED" | "FAILED";
export interface JobRequirementVersion {
  id: number;
  jobRequirementId: string;
  version: number;
  source: string;
  contentHash: string;
  parseStatus: JobRequirementParseStatus;
  createdAt: string;
  updatedAt: string;
}
export interface JobRequirementParseResult {
  version: JobRequirementVersion;
  indexingIdempotencyKey: string;
  chunkCount: number;
  requirementCount: number;
}
export interface StructuredJobRequirement {
  id: number;
  jobRequirementVersionId: number;
  skillCode: string;
  importance: number;
  requiredLevel: string;
  requirementText: string;
  sourceChunkId: number;
  createdAt: string;
}
export interface JobSearchResult {
  chunkId: number;
  score: number;
  rank: number;
  section: string;
  contentHash: string;
  sourceReference: string;
}

export type GapStatus = "MATCH" | "GAP" | "UNKNOWN";
export type EvidenceStrength = "NONE" | "WEAK" | "MODERATE" | "STRONG";
export interface GapSummary {
  totalRequirements: number;
  matchedRequirements: number;
  gapRequirements: number;
  unknownRequirements: number;
  weightedMatchRate: number;
  criticalGapCount: number;
}
export interface GapEvidenceReference {
  evidenceId: number;
  resumeVersionId: number;
  skillCode: string;
  claim: string;
  sourceReference: string;
  confidence: number;
}
export interface GapAnalysisItem {
  id: number;
  requirementId: number;
  skillCode: string;
  requirementText: string;
  importance: number;
  requiredLevel: string;
  status: GapStatus;
  candidateEvidence: GapEvidenceReference[];
  evidenceStrength: EvidenceStrength;
  confidence: number;
  reason: string;
  sourceChunkId: number;
}
export interface GapAnalysisResult {
  id: number;
  resumeVersionId: number;
  candidateProfileSnapshotId: number;
  jobRequirementVersionId: number;
  status: GapStatus;
  summary: GapSummary;
  items: GapAnalysisItem[];
  createdAt: string;
}

export interface RecommendationInsight {
  requirementId: number;
  skillCode: string;
  requirementText: string;
  importance: number;
  requiredLevel: string;
  status: GapStatus;
  evidenceStrength: EvidenceStrength;
  confidence: number;
  reason: string;
  sourceChunkId: number;
  candidateEvidence: GapEvidenceReference[];
}
export interface JobRecommendation {
  jobRequirementVersionId: number;
  jobRequirementId: string;
  jobVersion: number;
  recommendationScore: number;
  weightedMatchRate: number;
  matchCount: number;
  gapCount: number;
  unknownCount: number;
  criticalGapCount: number;
  summary: string;
  topMatches: RecommendationInsight[];
  topGaps: RecommendationInsight[];
  topUnknowns: RecommendationInsight[];
}
export interface RecommendationResponse {
  resumeVersionId: number;
  recommendations: JobRecommendation[];
}
