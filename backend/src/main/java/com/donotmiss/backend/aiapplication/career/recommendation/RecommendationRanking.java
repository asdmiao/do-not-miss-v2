package com.donotmiss.backend.aiapplication.career.recommendation;
import java.util.*;
/** Stable deterministic ordering shared by the application service and unit tests. */
public final class RecommendationRanking {
 private RecommendationRanking(){} public static List<RecommendationDtos.Item> top(List<RecommendationDtos.Item> items,int topK){return (items==null?List.<RecommendationDtos.Item>of():items).stream().sorted(Comparator.comparing(RecommendationDtos.Item::recommendationScore).reversed().thenComparing(RecommendationDtos.Item::jobRequirementVersionId)).limit(Math.max(1,topK)).toList();}
}
