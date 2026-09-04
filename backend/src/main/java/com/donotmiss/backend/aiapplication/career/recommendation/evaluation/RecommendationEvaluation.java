package com.donotmiss.backend.aiapplication.career.recommendation.evaluation;
import java.util.*;
/** Small ranking evaluator for human-labelled high-match jobs; no external evaluation infrastructure required. */
public final class RecommendationEvaluation {
 private RecommendationEvaluation(){} public static Metrics evaluate(List<Case> cases,Map<String,List<String>> rankedJobIds,int k){if(cases==null||cases.isEmpty())return new Metrics(0,0,0,0,0);int hit=0;double precision=0,recall=0,mrr=0;int limit=Math.max(1,k);for(Case item:cases){List<String> ranked=rankedJobIds.getOrDefault(item.candidateId(),List.of()).stream().limit(limit).toList();Set<String> relevant=new LinkedHashSet<>(item.highMatchJobIds());long matched=ranked.stream().filter(relevant::contains).count();if(matched>0)hit++;precision+=ranked.isEmpty()?0:(double)matched/ranked.size();recall+=relevant.isEmpty()?0:(double)matched/relevant.size();int rank=0;for(String id:ranked){rank++;if(relevant.contains(id)){mrr+=1d/rank;break;}}}int size=cases.size();return new Metrics(size,round((double)hit/size),round(precision/size),round(recall/size),round(mrr/size));}
 private static double round(double value){return Math.round(value*1000d)/1000d;} public record Case(String candidateId,List<String> highMatchJobIds){} public record Metrics(int candidateCount,double topKHitRate,double precisionAtK,double recallAtK,double mrr){}
}
