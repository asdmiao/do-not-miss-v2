package com.donotmiss.backend.aiapplication.career.recommendation;

import com.donotmiss.backend.aiapplication.career.gap.GapAnalysisDtos;
import com.donotmiss.backend.aiapplication.career.gap.GapStatus;
import org.springframework.stereotype.Component;
import java.math.*; import java.util.*;

/** Deterministic ranking policy over Phase 2C facts. UNKNOWN reduces certainty only; it is never counted as a GAP. */
@Component
public class RecommendationPolicy {
 private static final BigDecimal CRITICAL_GAP_PENALTY=new BigDecimal("0.0800"); private static final BigDecimal UNKNOWN_PENALTY=new BigDecimal("0.0150");
 public Decision score(GapAnalysisDtos.Result analysis){GapAnalysisDtos.Summary summary=analysis.summary();BigDecimal raw=summary.weightedMatchRate().subtract(CRITICAL_GAP_PENALTY.multiply(BigDecimal.valueOf(summary.criticalGapCount()))).subtract(UNKNOWN_PENALTY.multiply(BigDecimal.valueOf(summary.unknownRequirements())));BigDecimal score=raw.max(BigDecimal.ZERO).min(BigDecimal.ONE).setScale(4,RoundingMode.HALF_UP);List<GapAnalysisDtos.Item> ordered=analysis.items()==null?List.of():analysis.items();List<GapAnalysisDtos.Item> matches=ordered.stream().filter(item->item.status()==GapStatus.MATCH).sorted(byImportance()).limit(3).toList();List<GapAnalysisDtos.Item> gaps=ordered.stream().filter(item->item.status()==GapStatus.GAP).sorted(byImportance()).limit(3).toList();List<GapAnalysisDtos.Item> unknowns=ordered.stream().filter(item->item.status()==GapStatus.UNKNOWN).sorted(byImportance()).limit(3).toList();return new Decision(score,summary.weightedMatchRate(),summary.matchedRequirements(),summary.gapRequirements(),summary.unknownRequirements(),summary.criticalGapCount(),matches,gaps,unknowns,reason(matches,gaps,unknowns,summary));}
 private Comparator<GapAnalysisDtos.Item> byImportance(){return Comparator.comparingInt(GapAnalysisDtos.Item::importance).reversed().thenComparing(GapAnalysisDtos.Item::skillCode);}
 private String reason(List<GapAnalysisDtos.Item> matches,List<GapAnalysisDtos.Item> gaps,List<GapAnalysisDtos.Item> unknowns,GapAnalysisDtos.Summary summary){String strengths=join(matches), blockers=join(gaps), uncertain=join(unknowns);String start=strengths.isBlank()?"No confirmed high-priority matches are available.":"Strong matches: "+strengths+".";String gap=blockers.isBlank()?" No confirmed capability gaps.":" Key gaps: "+blockers+".";String unknown=uncertain.isBlank()?"":" Unknown evidence: "+uncertain+".";return start+gap+unknown+" Weighted match rate is "+summary.weightedMatchRate().movePointRight(2).setScale(1,RoundingMode.HALF_UP)+"%.";}
 private String join(List<GapAnalysisDtos.Item> items){return String.join(", ",items.stream().map(GapAnalysisDtos.Item::skillCode).toList());}
 public record Decision(BigDecimal recommendationScore,BigDecimal weightedMatchRate,int matchCount,int gapCount,int unknownCount,int criticalGapCount,List<GapAnalysisDtos.Item> topMatches,List<GapAnalysisDtos.Item> topGaps,List<GapAnalysisDtos.Item> topUnknowns,String summary){}
}
