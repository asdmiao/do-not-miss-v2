package com.donotmiss.backend.aiapplication.retrieval.job.evaluation;

import com.donotmiss.backend.aiapplication.retrieval.job.JobRequirementRetrievalService;
import java.util.*;

/** Metric-only evaluator. Fixture setup supplies persisted version/chunk ids; no production retrieval assumptions leak in. */
public final class JobRetrievalEvaluation {
    private JobRetrievalEvaluation() { }
    public static Metrics evaluate(List<EvalCase> cases, Map<String,List<JobRequirementRetrievalService.JobRetrievalResult>> results, int k) {
        if(cases==null||cases.isEmpty()) return new Metrics(0,0,0,0);
        int safeK=Math.max(1,k); double recall=0,mrr=0,ndcg=0;
        for(EvalCase c:cases){List<JobRequirementRetrievalService.JobRetrievalResult> found=results.getOrDefault(c.id(),List.of());Set<Long> expected=new LinkedHashSet<>(c.relevanceByChunkId().keySet());int matched=0;double dcg=0;int first=0;for(int i=0;i<Math.min(safeK,found.size());i++){int relevance=Math.max(0,c.relevanceByChunkId().getOrDefault(found.get(i).chunkId(),0));if(relevance>0){matched++;if(first==0)first=i+1;dcg+=(Math.pow(2,relevance)-1)/(Math.log(i+2)/Math.log(2));}}recall+=expected.isEmpty()?0:(double)matched/expected.size();mrr+=first==0?0:1.0/first;List<Integer> ideal=c.relevanceByChunkId().values().stream().filter(v->v>0).sorted(Comparator.reverseOrder()).limit(safeK).toList();double idealDcg=0;for(int i=0;i<ideal.size();i++)idealDcg+=(Math.pow(2,ideal.get(i))-1)/(Math.log(i+2)/Math.log(2));ndcg+=idealDcg==0?0:dcg/idealDcg;}
        return new Metrics(cases.size(),round(recall/cases.size()),round(mrr/cases.size()),round(ndcg/cases.size()));
    }
    private static double round(double value){return Math.round(value*1000d)/1000d;}
    public record EvalCase(String id,String query,Map<Long,Integer> relevanceByChunkId) { public EvalCase { relevanceByChunkId=relevanceByChunkId==null?Map.of():Map.copyOf(relevanceByChunkId); } }
    public record Metrics(int caseCount,double recallAtK,double mrr,double nDCG) { }
}
