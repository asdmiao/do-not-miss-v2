package com.donotmiss.backend.aiapplication.career.gap;
import com.donotmiss.backend.aiapplication.career.job.StructuredJobRequirementEntity; import java.math.*; import java.util.*;
/** Deterministic summary calculator; no model output participates in these aggregate values. */
public final class GapAnalysisSummaryCalculator {
 private GapAnalysisSummaryCalculator(){}
 public static GapAnalysisDtos.Summary calculate(List<Input> inputs){int total=inputs==null?0:inputs.size(),match=0,gap=0,unknown=0,weight=0,matchedWeight=0,critical=0;for(Input input:inputs==null?List.<Input>of():inputs){int importance=input.importance();weight+=importance;if(input.status()==GapStatus.MATCH){match++;matchedWeight+=importance;}else if(input.status()==GapStatus.GAP){gap++;if(importance>=4)critical++;}else unknown++;}return new GapAnalysisDtos.Summary(total,match,gap,unknown,weight==0?BigDecimal.ZERO:BigDecimal.valueOf(matchedWeight).divide(BigDecimal.valueOf(weight),4,RoundingMode.HALF_UP),critical);}
 public record Input(int importance,GapStatus status){}
}
