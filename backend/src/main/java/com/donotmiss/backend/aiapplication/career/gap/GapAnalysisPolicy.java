package com.donotmiss.backend.aiapplication.career.gap;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

/** Java-owned final policy. Semantic output is an input signal, never a direct GapStatus. */
@Component
public class GapAnalysisPolicy {
 private static final BigDecimal MATCH_CONFIDENCE=new BigDecimal("0.7000"); private static final BigDecimal GAP_CONFIDENCE=new BigDecimal("0.7000");
 public GapDeterministicMatch deterministic(boolean hasEvidence,String candidateLevel,String requiredLevel){
  if(!hasEvidence)return GapDeterministicMatch.NO_EVIDENCE; CapabilityLevel candidate=CapabilityLevel.from(candidateLevel), required=CapabilityLevel.from(requiredLevel);
  if(required==CapabilityLevel.UNKNOWN||candidate==CapabilityLevel.UNKNOWN)return GapDeterministicMatch.SEMANTIC_REVIEW;
  return candidate.rank()>=required.rank()?GapDeterministicMatch.LEVEL_SATISFIED:GapDeterministicMatch.LEVEL_INSUFFICIENT;
 }
 public Decision decide(GapDeterministicMatch deterministic,GapSemanticAssessment semantic){
  if(deterministic==GapDeterministicMatch.NO_EVIDENCE)return new Decision(GapStatus.UNKNOWN,EvidenceStrength.NONE,BigDecimal.ZERO,"No candidate evidence exists for this skill code.");
  if(deterministic==GapDeterministicMatch.LEVEL_SATISFIED)return new Decision(GapStatus.MATCH,EvidenceStrength.STRONG,new BigDecimal("1.0000"),"Explicit candidate level satisfies the required level.");
  if(deterministic==GapDeterministicMatch.LEVEL_INSUFFICIENT)return new Decision(GapStatus.GAP,EvidenceStrength.WEAK,new BigDecimal("1.0000"),"Explicit candidate level is below the required level.");
  if(semantic==null)return new Decision(GapStatus.UNKNOWN,EvidenceStrength.NONE,BigDecimal.ZERO,"Semantic support was not available.");
  BigDecimal confidence=semantic.confidence()==null?BigDecimal.ZERO:semantic.confidence(); EvidenceStrength strength=semantic.evidenceStrength()==null?EvidenceStrength.NONE:semantic.evidenceStrength(); String reason=semantic.reason()==null||semantic.reason().isBlank()?"Semantic evidence was inconclusive.":semantic.reason();
  if(semantic.supported()&&strength==EvidenceStrength.STRONG&&confidence.compareTo(MATCH_CONFIDENCE)>=0)return new Decision(GapStatus.MATCH,strength,confidence,reason);
  if(!semantic.supported()&&confidence.compareTo(GAP_CONFIDENCE)>=0)return new Decision(GapStatus.GAP,strength,confidence,reason);
  return new Decision(GapStatus.UNKNOWN,strength,confidence,reason);
 }
 public record Decision(GapStatus status,EvidenceStrength evidenceStrength,BigDecimal confidence,String reason) { }
}
