package com.donotmiss.backend.aiapplication.career.gap.evaluation;
import com.donotmiss.backend.aiapplication.career.gap.GapStatus; import java.util.*;
/** Deterministic evaluator for fixture-level expected statuses; results come from a real GapAnalysis run. */
public final class GapAnalysisEvaluation {
 private GapAnalysisEvaluation(){} public static Metrics evaluate(List<Case> cases,Map<String,GapStatus> actual){if(cases==null||cases.isEmpty())return new Metrics(0,0,Map.of());int correct=(int)cases.stream().filter(item->actual.get(item.id())==item.expected()).count();Map<GapStatus,ClassMetrics> byStatus=new EnumMap<>(GapStatus.class);for(GapStatus status:GapStatus.values()){int tp=0,fp=0,fn=0;for(Case item:cases){GapStatus got=actual.get(item.id());if(got==status&&item.expected()==status)tp++;else if(got==status)fp++;else if(item.expected()==status)fn++;}byStatus.put(status,new ClassMetrics(tp,fp,fn,ratio(tp,tp+fp),ratio(tp,tp+fn)));}return new Metrics(cases.size(),ratio(correct,cases.size()),Map.copyOf(byStatus));}
 private static double ratio(int n,int d){return d==0?0:Math.round((double)n/d*1000d)/1000d;} public record Case(String id,GapStatus expected){} public record ClassMetrics(int truePositive,int falsePositive,int falseNegative,double precision,double recall){} public record Metrics(int caseCount,double statusAccuracy,Map<GapStatus,ClassMetrics> byStatus){}
}
