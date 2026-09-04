import { api } from "@/api/client";
import type { CandidateEvidence, CandidateProfile, GapAnalysisResult, JobRequirementParseResult, JobRequirementVersion, JobSearchResult, RecommendationResponse, ResumeVersion, StructuredJobRequirement } from "@/types/career";

export const careerApi = {
  versions() { return api.get<ResumeVersion[]>("/api/career/resumes"); },
  create(content: string, fileReference = "", resumeId?: string) {
    return api.post<ResumeVersion>("/api/career/resumes", { content, fileReference, resumeId });
  },
  parse(resumeVersionId: number) { return api.post<CandidateProfile>(`/api/career/resumes/${resumeVersionId}/parse`); },
  profile(resumeVersionId: number) { return api.get<CandidateProfile>(`/api/career/resumes/${resumeVersionId}/profile`); },
  evidence(resumeVersionId: number) { return api.get<CandidateEvidence[]>(`/api/career/resumes/${resumeVersionId}/evidences`); }
};

export const jobRequirementApi = {
  versions() { return api.get<JobRequirementVersion[]>("/api/career/jobs"); },
  create(content: string, source = "TEXT", jobRequirementId?: string) {
    return api.post<JobRequirementVersion>("/api/career/jobs", { content, source, jobRequirementId });
  },
  parse(jobRequirementVersionId: number) { return api.post<JobRequirementParseResult>(`/api/career/jobs/${jobRequirementVersionId}/parse`); },
  requirements(jobRequirementVersionId: number) { return api.get<StructuredJobRequirement[]>(`/api/career/jobs/${jobRequirementVersionId}/requirements`); },
  search(jobRequirementVersionId: number, query: string) {
    return api.post<{ results: JobSearchResult[]; mode: string }>(`/api/career/jobs/${jobRequirementVersionId}/search`, { query, limit: 10 });
  },
  analyzeGap(jobRequirementVersionId: number, resumeVersionId: number) {
    return api.post<GapAnalysisResult>(`/api/career/jobs/${jobRequirementVersionId}/gap-analysis`, { resumeVersionId });
  },
  gapAnalysis(jobRequirementVersionId: number, resumeVersionId: number) {
    return api.get<GapAnalysisResult>(`/api/career/jobs/${jobRequirementVersionId}/gap-analysis?resumeVersionId=${resumeVersionId}`);
  }
};

export const recommendationApi = {
  recommend(resumeVersionId: number, jobRequirementVersionIds: number[], topK = 3) {
    return api.post<RecommendationResponse>("/api/career/recommendations", { resumeVersionId, jobRequirementVersionIds, topK });
  }
};
